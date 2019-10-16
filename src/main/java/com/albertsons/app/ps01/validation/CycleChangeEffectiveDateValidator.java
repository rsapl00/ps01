package com.albertsons.app.ps01.validation;

import static com.albertsons.app.ps01.util.DateUtil.*;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.BufferDayEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.domain.resource.ValidEffectiveDates;
import com.albertsons.app.ps01.exception.InvalidEffectiveDate;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.util.DateUtil;

public class CycleChangeEffectiveDateValidator extends Validator {

    public CycleChangeEffectiveDateValidator(CycleChangeRequestRepository repository) {
        super(repository);
    }

    public CycleChangeEffectiveDateValidator(CycleChangeRequestRepository repository, Validator validator) {
        super(repository, validator);
    }

    @Override
    public Boolean isValid(final CycleChangeRequestDTO dto) {

        if (validator != null && !validator.isValid(dto)) {
            return false;
        }

        // final Boolean isAddAction = dto.getId().equals(Long.valueOf(0l)) ? true : false;

        return validateEffectiveDate(dto);
    }

    private boolean validateEffectiveDate(final CycleChangeRequestDTO dto) {

        Date runDate = dto.getRunDate();

        List<CycleChangeRequest> prevWeek = repository.findActiveByDivIdAndBetweenRunDatesDesc(dto.getDivId(),
                DateUtil.getBufferDate(runDate, BufferDayEnum.MINUS_BUFFER), runDate, DateUtil.getExpiryTimestamp());

        List<CycleChangeRequest> nextWeek = repository.findActiveByDivIdAndBetweenRunDatesAsc(dto.getDivId(), runDate,
                DateUtil.getBufferDate(runDate, BufferDayEnum.PLUS_BUFFER), DateUtil.getExpiryTimestamp());

        return validateCycleChangeEffectiveDate(prevWeek, nextWeek, dto);
    }

    private Boolean validateCycleChangeEffectiveDate(final List<CycleChangeRequest> beforeCycleChanges,
            final List<CycleChangeRequest> afterCycleChanges, final CycleChangeRequestDTO dto) {

        ValidEffectiveDates before = processEffectiveDateFromEarlierEffectiveDates(beforeCycleChanges, dto);
        ValidEffectiveDates after = processEffectiveDateFromLaterEffectiveDates(afterCycleChanges, dto);

        boolean isBothValid = before.IsValid() && after.IsValid();

        int sameEffDtCount = before.getEffectiveDates().stream().filter(date -> {
            return DateUtil.isEqual(date, dto.getEffectiveDate());
        }).collect(Collectors.toList()).size() + after.getEffectiveDates().stream().filter(date -> {
            return DateUtil.isEqual(date, dto.getEffectiveDate());
        }).collect(Collectors.toList()).size();

        // if (sameEffDtCount >= RunSequenceEnum.SECOND.getRunSequence() && isAddAction)
        // {
        if (sameEffDtCount >= RunSequenceEnum.SECOND.getRunSequence()) {
            // TODO: messaging template
            throw new InvalidEffectiveDate(
                    "Invalid effective date. Only two consecutive same effective date is valid.");
        }

        if (sameEffDtCount <= 1 && isBothValid) {
            return true;
            // } else if (sameEffDtCount == 2 && isBothValid && !isAddAction) {
            // return true;
        }

        return false;
    }

    private ValidEffectiveDates processEffectiveDateFromLaterEffectiveDates(
            final List<CycleChangeRequest> ascOrderCycleChange, final CycleChangeRequestDTO dto) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : ascOrderCycleChange) {

            // skip own record
            if (cycle.getId().equals(dto.getId())) {
                continue;
            }

            // skip same run date since same run date has been processed in
            // processEffectiveDateFromEarlierEffectiveDates
            if (DateUtil.isEqual(cycle.getRunDate(), dto.getRunDate())) {
                continue;
            }

            final Date subEffDt = dto.getEffectiveDate();
            final Date nextEffDt = cycle.getEffectiveDate();

            // if submitted eff date is earlier than previous cycle's effective date.
            if (DateUtil.isBefore(subEffDt, nextEffDt)) {
                validEffDate.setIsValid(true);
                break;
            }

            // if submitted effective date is later than the previous cycle's
            // effective date
            if (isAfter(subEffDt, nextEffDt)) {
                if (validEffDate.getEffectiveDates().isEmpty()) {
                    validEffDate.setIsValid(false);
                }
                break;
            }

            // count the effective date
            if (isEqual(subEffDt, nextEffDt)) {
                validEffDate.addEffectiveDate(subEffDt);
            }

            int sameEffDtCount = validEffDate.getEffectiveDates().stream().filter(date -> {
                return isEqual(date, subEffDt);
            }).collect(Collectors.toList()).size();

            if (sameEffDtCount >= RunSequenceEnum.SECOND.getRunSequence()) {
                validEffDate.setIsValid(false);
                break;
            }
        }

        return validEffDate;

    }

    private ValidEffectiveDates processEffectiveDateFromEarlierEffectiveDates(
            final List<CycleChangeRequest> descOrderCycleChange, final CycleChangeRequestDTO dto) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : descOrderCycleChange) {

            // skip own record for validation
            if (cycle.getId().equals(dto.getId())) {
                continue;
            }

            final Date subEffDt = dto.getEffectiveDate();
            final Date subRunDt = dto.getRunDate();
            final Date prevEffDt = cycle.getEffectiveDate();
            final Date prevRunDt = cycle.getRunDate();

            if (isEqual(subRunDt, prevRunDt) && isBefore(subEffDt, prevEffDt)) {
                if (cycle.getRunNumber().equals(RunSequenceEnum.FIRST.getRunSequence())) {
                    validEffDate.setIsValid(false);
                    break;
                }

                validEffDate.addEffectiveDate(subEffDt);
            } else if (isBefore(subEffDt, prevEffDt)) {
                // if submitted effective date is earlier than the previous cycle's
                // effective date
                if (validEffDate.getEffectiveDates().isEmpty()) {
                    validEffDate.setIsValid(false);
                }
                break;
            }

            // count the effective date
            if (isEqual(subEffDt, prevEffDt)) {
                validEffDate.addEffectiveDate(subEffDt);
            }

            int sameEffDtCount = validEffDate.getEffectiveDates().stream().filter(date -> {
                return isEqual(date, subEffDt);
            }).collect(Collectors.toList()).size();

            if (sameEffDtCount >= RunSequenceEnum.SECOND.getRunSequence()) {
                validEffDate.setIsValid(false);
                break;
            }
        }

        return validEffDate;
    }

}