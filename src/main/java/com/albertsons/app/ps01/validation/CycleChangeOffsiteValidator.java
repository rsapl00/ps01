package com.albertsons.app.ps01.validation;

import java.util.List;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.exception.CycleChangeNotFoundException;
import com.albertsons.app.ps01.exception.CycleChangeRequestOffsiteException;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.util.DateUtil;

public class CycleChangeOffsiteValidator extends Validator {

    public CycleChangeOffsiteValidator(CycleChangeRequestRepository repository) {
        super(repository);
    }

    public CycleChangeOffsiteValidator(CycleChangeRequestRepository repository, Validator validator) {
        super(repository, validator);
    }

    @Override
    public Boolean isValid(CycleChangeRequestDTO dto) {

        if (validator != null && !validator.isValid(dto)) {
            return false;
        }

        // retrieve cycle change request by run date and not expired.
        final List<CycleChangeRequest> cycleChangeRequests = repository
                .findByDivIdAndRunDateAndNotExpired(dto.getDivId(), dto.getRunDate(), DateUtil.getExpiryTimestamp());

        if (cycleChangeRequests.isEmpty()) {
            return true;
        }

        final boolean isAddAction = dto.getId().equals(Long.valueOf(0l)) ? true : false;

        boolean isValid = true;
        if (!isAddAction) {
            repository.findById(dto.getId()).map(toBeUpdatedCycle -> {

                cycleChangeRequests.stream().forEach(cycle -> {
                    if (!cycle.getId().equals(toBeUpdatedCycle.getId())) {

                        if (cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                            if (cycle.getRunNumber().equals(RunSequenceEnum.SECOND.getRunSequence())) {
                                if (dto.getOffsiteIndicator().equals(OffsiteIndicatorEnum.OFFSITE.getIndicator())) {
                                    throw new CycleChangeRequestOffsiteException(
                                            "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. "
                                                    + "Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                                }
                            }
                        } else if (cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.OFFSITE.getIndicator())) {
                            if (cycle.getRunNumber().equals(RunSequenceEnum.FIRST.getRunSequence())) {
                                if (dto.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                                    throw new CycleChangeRequestOffsiteException(
                                            "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. "
                                                    + "Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                                }
                            }
                        }
                    }
                });

                return toBeUpdatedCycle;
            }).orElseThrow(() -> {
                throw new CycleChangeNotFoundException();
            });

            isValid = true;
        } else

        {
            for (CycleChangeRequest cycle : cycleChangeRequests) {
                // skip self
                if (cycle.getId().equals(dto.getId())) {
                    continue;
                }

                // 2. If There is Run 1 non-offsite.
                // 2.1 Insert Run 2 - request is valid.
                if ((cycle.getRunNumber().intValue() == RunSequenceEnum.FIRST.getRunSequence())
                        && (cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator()))) {

                    if (dto.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                        break;
                    }

                    throw new CycleChangeRequestOffsiteException(
                            "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                }

                // 3. if there is run 1 Offsite
                // 3.1 Insert Run 2 if it is Offsite
                // 3.2 Throw error if Run 2 is not offsite.
                if ((RunSequenceEnum.FIRST.getRunSequence() == cycle.getRunNumber().intValue())
                        && cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.OFFSITE.getIndicator())) {

                    // if Run 1 is OFFSITE, RUN 2 should also be OFFSITE. else throw Exception
                    if (dto.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                        // TODO: Messaging template
                        throw new CycleChangeRequestOffsiteException(
                                "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                    }

                    break;
                }
            }
        }

        return isValid;
    }

}