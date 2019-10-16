package com.albertsons.app.ps01.validation;

import static com.albertsons.app.ps01.util.DateUtil.*;

import java.util.List;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.exception.InvalidEffectiveDate;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.util.DateUtil;

public class CycleChangeRunSequenceValidator extends Validator {

    public CycleChangeRunSequenceValidator(CycleChangeRequestRepository repository) {
        super(repository);
    }

    public CycleChangeRunSequenceValidator(CycleChangeRequestRepository repository, Validator validator) {
        super(repository, validator);
    }

    @Override
    public Boolean isValid(CycleChangeRequestDTO dto) {

        if (validator != null && !validator.isValid(dto)) {
            return false;
        }

        final List<CycleChangeRequest> existingCycles = repository.findByDivIdAndRunDateAndNotExpired(dto.getDivId(),
                dto.getRunDate(), DateUtil.getExpiryTimestamp());

        repository.findById(dto.getId()).map(toBeUpdatedCycle -> {

            if (isEqual(toBeUpdatedCycle.getRunDate(), dto.getRunDate())) {

                existingCycles.stream().forEach(cycle -> {
                    if (!cycle.getId().equals(toBeUpdatedCycle.getId())) {
                        if (cycle.getRunNumber().equals(RunSequenceEnum.SECOND.getRunSequence())) {
                            if (isAfter(dto.getEffectiveDate(), cycle.getEffectiveDate())) {
                                throw new InvalidEffectiveDate(
                                        "Effective Date of Run 2 is later than the request's effective date.");
                            }
                        }
                    }
                });
            }
            return toBeUpdatedCycle;
        });

        return true;
    }

}