package com.albertsons.app.ps01.validation;

import static com.albertsons.app.ps01.util.CycleScheduleUtility.*;
import static com.albertsons.app.ps01.util.DateUtil.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.enums.BufferDayEnum;
import com.albertsons.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.domain.resource.ValidEffectiveDates;
import com.albertsons.app.ps01.exception.CycleChangeRequestOffsiteException;
import com.albertsons.app.ps01.exception.InvalidEffectiveDate;
import com.albertsons.app.ps01.exception.MaximumRunSchedulePerRunDateException;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.util.DateUtil;
import com.google.gson.Gson;

public class CycleChangeRequestValidator
        implements ConstraintValidator<CycleChangeRequestConstraint, CycleChangeRequestDTO> {

    private CycleChangeRequestRepository repository;

    public CycleChangeRequestValidator(CycleChangeRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize(CycleChangeRequestConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(final CycleChangeRequestDTO cycleChangeDTO, ConstraintValidatorContext context) {
        try {

            final List<Boolean> isAddAction = new ArrayList<>(1);
            if (cycleChangeDTO.getId().equals(Long.valueOf(0l))) {
                isAddAction.add(true);
            } else {
                isAddAction.add(false);
            }

            // retrieve cycle change request by run date and not expired.
            final List<CycleChangeRequest> cycleChangeRequests = repository.findByDivIdAndRunDateAndNotExpired(
                    cycleChangeDTO.getDivId(), cycleChangeDTO.getRunDate(), DateUtil.getExpiryTimestamp());

            if (cycleChangeRequests.isEmpty()) {
                return true;
            }
            
            if (cycleChangeRequests.size() >= RunSequenceEnum.SECOND.getRunSequence() && isAddAction.get(0)) {
                // TODO: messaging template
                throw new MaximumRunSchedulePerRunDateException(
                        "Maximum schedule per run date reached. Only two (2) same run date is accepted.");
            }

            Gson gson = new Gson();
            CycleChangeRequest request = gson.fromJson(gson.toJson(cycleChangeDTO), CycleChangeRequest.class);

            boolean isValid = cycleChangeRequests.stream().anyMatch(cycle -> {

                if (isEqual(cycle.getRunDate(), request.getRunDate())
                        && isAfter(cycle.getEffectiveDate(), request.getEffectiveDate())) {
                    // TODO: messaging template
                    throw new InvalidEffectiveDate("Invalid Effective Date. "
                            + "Specified run date has an effective date that is later than the request.");
                }

                if (!validateEffectiveDate(request, isAddAction.get(0))) {
                    throw new InvalidEffectiveDate(
                            "Invalid effective date. Review your request and compare it from the current schedule.");
                }

                // 2. If There is Run 1 non-offsite.
                // 2.1 Insert Run 2.
                if ((RunSequenceEnum.FIRST.getRunSequence() == cycle.getRunNumber().intValue())
                        && cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                    return true;
                }

                // 3. if there is run 1 Offsite
                // 3.1 Insert Run 2 if it is Offsite
                // 3.2 Throw error if Run 2 is not offsite.
                if ((RunSequenceEnum.FIRST.getRunSequence() == cycle.getRunNumber().intValue())
                        && cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.OFFSITE.getIndicator())) {

                    // if Run 1 is OFFSITE, RUN 2 should also be OFFSITE. else throw Exception
                    if (request.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                        // TODO: Messaging template
                        throw new CycleChangeRequestOffsiteException(
                                "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                    }

                    return true;
                }

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid Cycle Change Request.").addConstraintViolation();

                return false;
            });

            return isValid;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();

            return false;
        }
    }

    private boolean validateEffectiveDate(final CycleChangeRequest submittedCycleChange, final Boolean isAddAction) {

        Date runDate = submittedCycleChange.getRunDate();

        List<CycleChangeRequest> prevWeek = repository.findActiveByDivIdAndBetweenRunDatesDesc(
                submittedCycleChange.getDivId(), DateUtil.getBufferDate(runDate, BufferDayEnum.MINUS_BUFFER), runDate,
                DateUtil.getExpiryTimestamp());

        List<CycleChangeRequest> nextWeek = repository.findActiveByDivIdAndBetweenRunDatesAsc(
                submittedCycleChange.getDivId(), runDate, DateUtil.getBufferDate(runDate, BufferDayEnum.PLUS_BUFFER),
                DateUtil.getExpiryTimestamp());

        return validateCycleChangeEffectiveDate(prevWeek, nextWeek, submittedCycleChange, isAddAction);
    }

    private Boolean validateCycleChangeEffectiveDate(final List<CycleChangeRequest> beforeCycleChanges,
            final List<CycleChangeRequest> afterCycleChanges, final CycleChangeRequest submittedCycleChange, final Boolean isAddAction) {

        ValidEffectiveDates before = processEffectiveDateFromEarlierEffectiveDates(beforeCycleChanges,
                submittedCycleChange);

        ValidEffectiveDates after = processEffectiveDateFromLaterEffectiveDates(afterCycleChanges,
                submittedCycleChange);

        boolean isBothValid = before.IsValid() && after.IsValid();

        int sameEffDtCount = before.getEffectiveDates().stream().filter(date -> {
            return DateUtil.isEqual(date, submittedCycleChange.getEffectiveDate());
        }).collect(Collectors.toList()).size() + after.getEffectiveDates().stream().filter(date -> {
            return DateUtil.isEqual(date, submittedCycleChange.getEffectiveDate());
        }).collect(Collectors.toList()).size();

        if (sameEffDtCount >= MAXIMUM_SAME_EFFECTIVE_DATE && isAddAction) {
            // TODO: messaging template
            throw new InvalidEffectiveDate(
                    "Invalid effective date. Only two consecutive same effective date is valid.");
        }

        if (sameEffDtCount <= 1 && isBothValid) {
            return true;
        } else if (sameEffDtCount == 2 && isBothValid && !isAddAction) {
            return true;
        }

        return false;
    }

    private ValidEffectiveDates processEffectiveDateFromLaterEffectiveDates(
            final List<CycleChangeRequest> ascOrderCycleChange, final CycleChangeRequest submittedCycleChange) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : ascOrderCycleChange) {

            // skip same run date since same run date has been processed in
            // processEffectiveDateFromEarlierEffectiveDates
            if (DateUtil.isEqual(cycle.getRunDate(), submittedCycleChange.getRunDate())) {
                continue;
            }

            final Date subEffDt = submittedCycleChange.getEffectiveDate();
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

            if (sameEffDtCount >= MAXIMUM_SAME_EFFECTIVE_DATE) {
                validEffDate.setIsValid(false);
                break;
            }
        }

        return validEffDate;

    }

    private ValidEffectiveDates processEffectiveDateFromEarlierEffectiveDates(
            final List<CycleChangeRequest> descOrderCycleChange, final CycleChangeRequest submittedCycleChange) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : descOrderCycleChange) {
            final Date subEffDt = submittedCycleChange.getEffectiveDate();
            final Date prevEffDt = cycle.getEffectiveDate();

            // if submitted effective date is earlier than the previous cycle's
            // effective date
            if (isBefore(subEffDt, prevEffDt)) {
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

            if (sameEffDtCount >= MAXIMUM_SAME_EFFECTIVE_DATE) {
                validEffDate.setIsValid(false);
                break;
            }
        }

        return validEffDate;
    }
}