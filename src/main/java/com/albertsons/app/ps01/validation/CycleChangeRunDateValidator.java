package com.albertsons.app.ps01.validation;

import static com.albertsons.app.ps01.util.DateUtil.isEqual;

import java.time.LocalDate;
import java.util.List;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.exception.CycleChangeNotFoundException;
import com.albertsons.app.ps01.exception.MaximumRunSchedulePerRunDateException;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.util.DateUtil;

public class CycleChangeRunDateValidator extends Validator {

    public CycleChangeRunDateValidator(CycleChangeRequestRepository repository) {
        super(repository);
    }

    public CycleChangeRunDateValidator(CycleChangeRequestRepository repository, Validator validator) {
        super(repository, validator);
    }


    /**
     * This validates the run date of the requested cycle change.
     * 
     * Run date should be 7 days after the current date.
     * 
     * For modification of Cycle Change, Run date will be included in the request
     * but should not / can not be modified. Only Effective Date should be allowed
     * to be modified.
     * 
     * For addition of new Cycle Change, Run Date is also included and will be
     * validated the specified run date reached maximum allowed (2) per week.
     */
    @Override
    public Boolean isValid(final CycleChangeRequestDTO dto) {

        if (validator != null && !validator.isValid(dto)) {
            return false;
        }

        LocalDate date = dto.getRunDate().toLocalDate();
        LocalDate currentDate = LocalDate.now();

        if (date.isBefore(currentDate.plusDays(7))) {
            return false;
        }

        // This will assume that if Id is set to 0 in the request then the request is
        // for new cycle change
        final boolean isAddAction = dto.getId().equals(Long.valueOf(0l)) ? true : false;
        
        if (isAddAction) {
            final List<CycleChangeRequest> cycleChangeRequests = repository.findByDivIdAndRunDateAndNotExpired(
                    dto.getDivId(), dto.getRunDate(), DateUtil.getExpiryTimestamp());

            if (cycleChangeRequests.isEmpty()) {
                return true;
            }

            int count = cycleChangeRequests.stream().mapToInt(cycle -> {
                if (!cycle.getId().equals(dto.getId())) {
                    return 1;
                }
                return 0;
            }).sum();

            if (count >= RunSequenceEnum.SECOND.getRunSequence()) {
                throw new MaximumRunSchedulePerRunDateException(
                        "Maximum schedule per run date reached. Only two (2) same run date is accepted.");
            }

        } else {
            repository.findById(dto.getId()).map(cycle -> {
                if (!(isEqual(cycle.getRunDate(), dto.getRunDate()))) {
                    throw new RuntimeException("Run date is not modifiable.");
                }
                return cycle;
            }).orElseThrow(
                () -> new CycleChangeNotFoundException("Cycle Change not found.")
            );
        }

        return true;
    }
}