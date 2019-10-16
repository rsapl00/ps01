package com.albertsons.app.ps01.validation;

import java.util.List;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
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

        cycleChangeRequests.stream().forEach(cycle -> {

            if (!cycle.getId().equals(dto.getId())) {

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
        return true;
    }

}