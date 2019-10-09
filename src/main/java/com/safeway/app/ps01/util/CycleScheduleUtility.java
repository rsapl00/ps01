package com.safeway.app.ps01.util;

import static com.safeway.app.ps01.util.DateUtil.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.domain.enums.ChangeStatusEnum;
import com.safeway.app.ps01.domain.enums.CorpEnum;
import com.safeway.app.ps01.domain.enums.CycleChangeRequestTypeEnum;
import com.safeway.app.ps01.domain.enums.DayEnum;
import com.safeway.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.safeway.app.ps01.domain.enums.RunSequenceEnum;
import com.safeway.app.ps01.domain.resource.ValidEffectiveDates;
import com.safeway.app.ps01.exception.InvalidEffectiveDate;

public final class CycleScheduleUtility {

    public final static Integer MAXIMUM_SAME_EFFECTIVE_DATE = 2;

    public static boolean isRunDateExists(final LocalDate runDate, final List<CycleChangeRequest> cycleChangeRequests) {
        return cycleChangeRequests.stream().anyMatch(change -> {
            return change.getRunDate().equals(java.sql.Date.valueOf(runDate));
        });
    }

    public static List<CycleChangeRequest> generateCycleChangeRequest(final CycleSchedule cycleSchedule,
            final LocalDate currentDateInLoop) {

        final List<CycleChangeRequest> cycleChangeRequests = new ArrayList<>();

        // Compare by name: current date in loop to cycle schedule date
        if (DayEnum.getDayEnum(cycleSchedule.getDayNum())
                .equals(DayEnum.getDayEnum(currentDateInLoop.getDayOfWeek()))) {
            final DayEnum runDay = DayEnum.getDayEnum(cycleSchedule.getDayNum());

            final DayEnum defEffectiveDateOne = DayEnum.getDayEnum(cycleSchedule.getDefOneEffectiveDayNbr());
            final DayEnum defEffectiveDateTwo = DayEnum.getDayEnum(cycleSchedule.getDefTwoEffectiveDayNbr());

            final String offSiteOneInd = cycleSchedule.getDefaultRunOneOsInd();
            final String offSiteTwoInd = cycleSchedule.getDefaultRunTwoOsInd();

            // if Default Effective Date 1 and 2 is NOT 0 or has run date
            if (!defEffectiveDateOne.equals(DayEnum.NO_RUNDAY)) {
                cycleChangeRequests.add(createCycleChangeRequest(cycleSchedule, currentDateInLoop, runDay,
                        defEffectiveDateOne, offSiteOneInd, RunSequenceEnum.FIRST));

                if (!defEffectiveDateTwo.equals(DayEnum.NO_RUNDAY)) {
                    cycleChangeRequests.add(createCycleChangeRequest(cycleSchedule, currentDateInLoop, runDay,
                            defEffectiveDateTwo, offSiteTwoInd, RunSequenceEnum.SECOND));
                }
            }
        }

        return cycleChangeRequests;
    }

    public static CycleChangeRequest createNewCycleChangeRequest(final CycleChangeRequest submittedCycleChange,
            RunSequenceEnum runSequence) {

        Gson gson = new Gson();
        CycleChangeRequest newChangeRequest = gson.fromJson(gson.toJson(submittedCycleChange),
                CycleChangeRequest.class);

        /*
         * newChangeRequest.setDivId(user.getDivision()); division should be included on
         * the submitted request
         */
        newChangeRequest.setCorpId(CorpEnum.DEFAULT_CORP.getCorpId());
        newChangeRequest.setCreateTimestamp(now()); // TODO: REMOVE as this will be populated by Spring Data
                                                    // JPA @CreatedDate

        newChangeRequest.setRunNumber(runSequence.getRunSequence());
        newChangeRequest.setRunDayName(getDayName(newChangeRequest.getRunDate()));
        newChangeRequest.setEffectiveDayName(getDayName(newChangeRequest.getEffectiveDate()));

        if (newChangeRequest.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
            newChangeRequest.setCycleChangeRequestType(CycleChangeRequestTypeEnum.ADD.getRequestType());
        } else {
            newChangeRequest.setCycleChangeRequestType(CycleChangeRequestTypeEnum.ADD_OFFSITE.getRequestType());
        }

        newChangeRequest.setChangeStatusName(ChangeStatusEnum.SAVED.getChangeStatus());

        // TODO: This can be removed since the table column in DB2 has default value
        newChangeRequest.setExpiryTimestamp(getExpiryTimestamp());

        return newChangeRequest;
    }

    public static Boolean validateCycleChangeEffectiveDate(final List<CycleChangeRequest> beforeCycleChanges,
            final List<CycleChangeRequest> afterCycleChanges, final CycleChangeRequest submittedCycleChange) {

        // final List<CycleChangeRequest> descOrderCycleChange =
        // beforeCycleChanges.stream()
        // .sorted(Comparator.comparing(CycleChangeRequest::getEffectiveDate)).collect(Collectors.toList());

        // final List<CycleChangeRequest> ascOrderCycleChange =
        // afterCycleChanges.stream()
        // .sorted(Comparator.comparing(CycleChangeRequest::getEffectiveDate)).collect(Collectors.toList());

        ValidEffectiveDates before = processEffectiveDateFromEarlierEffectiveDates(beforeCycleChanges,
                submittedCycleChange);

        ValidEffectiveDates after = processEffectiveDateFromLaterEffectiveDates(afterCycleChanges,
                submittedCycleChange);

        boolean isBothValid = before.IsValid() && after.IsValid();

        int sameEffDtCount = before.getEffectiveDates().stream().filter(date -> {
            return isEqual(date, submittedCycleChange.getEffectiveDate());
        }).collect(Collectors.toList()).size() + after.getEffectiveDates().stream().filter(date -> {
            return isEqual(date, submittedCycleChange.getEffectiveDate());
        }).collect(Collectors.toList()).size();

        if (sameEffDtCount >= MAXIMUM_SAME_EFFECTIVE_DATE) {
            // TODO: messaging template
            throw new InvalidEffectiveDate("Invalid effective date. Only two same effective date per week are valid.");
        }

        if (sameEffDtCount <= 1 && isBothValid) {
            return true;
        }

        return false;
    }

    private static ValidEffectiveDates processEffectiveDateFromLaterEffectiveDates(
            final List<CycleChangeRequest> ascOrderCycleChange, final CycleChangeRequest submittedCycleChange) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : ascOrderCycleChange) {

            // skip same run date since same run date has been processed in
            // processEffectiveDateFromEarlierEffectiveDates
            if (isEqual(cycle.getRunDate(), submittedCycleChange.getRunDate())) {
                continue;
            }

            final Date subEffDt = submittedCycleChange.getEffectiveDate();
            final Date nextEffDt = cycle.getEffectiveDate();

            // if submitted eff date is earlier than previous cycle's effective date.
            if (isBefore(subEffDt, nextEffDt)) {
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

    private static ValidEffectiveDates processEffectiveDateFromEarlierEffectiveDates(
            final List<CycleChangeRequest> descOrderCycleChange, final CycleChangeRequest submittedCycleChange) {

        ValidEffectiveDates validEffDate = new ValidEffectiveDates();

        for (CycleChangeRequest cycle : descOrderCycleChange) {
            final Date subEffDt = submittedCycleChange.getEffectiveDate();
            final Date prevEffDt = cycle.getEffectiveDate();

            // if submitted eff date is later than previous cycle's effective date.
            // if (isAfter(subEffDt, prevEffDt)) {
            //     validEffDate.setIsValid(true);
            //     break;
            // }

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

    private static CycleChangeRequest createCycleChangeRequest(final CycleSchedule cycleSchedule,
            final LocalDate runDate, final DayEnum runDay, final DayEnum defEffectiveDate, final String offsiteInd,
            RunSequenceEnum sequence) {

        CycleChangeRequest schedule = new CycleChangeRequest();

        schedule.setCorpId(CorpEnum.DEFAULT_CORP.getCorpId());
        schedule.setDivId(cycleSchedule.getDivId());
        schedule.setRunDate(java.sql.Date.valueOf(runDate));

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        schedule.setCreateTimestamp(ts);

        schedule.setEffectiveDate(getEffectiveDate(runDate, defEffectiveDate));
        schedule.setEffectiveDayName(getDayName(schedule.getEffectiveDate()));

        schedule.setRunDayName(runDay.getDayName());
        schedule.setRunNumber(sequence.getRunSequence());
        schedule.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
        schedule.setOffsiteIndicator(offsiteInd);
        schedule.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());

        // TODO: This can be removed since the table column in DB2 has default value
        schedule.setExpiryTimestamp(getExpiryTimestamp());

        return schedule;
    }
}