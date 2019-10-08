package com.safeway.app.ps01.util;

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

public final class CycleScheduleUtility {

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
        newChangeRequest.setCreateTimestamp(DateUtil.now()); // TODO: REMOVE as this will be populated by Spring Data
                                                             // JPA @CreatedDate

        newChangeRequest.setRunNumber(runSequence.getRunSequence());
        newChangeRequest.setRunDayName(DateUtil.getDayName(newChangeRequest.getRunDate()));
        newChangeRequest.setEffectiveDayName(DateUtil.getDayName(newChangeRequest.getEffectiveDate()));

        if (newChangeRequest.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
            newChangeRequest.setCycleChangeRequestType(CycleChangeRequestTypeEnum.ADD.getRequestType());
        } else {
            newChangeRequest.setCycleChangeRequestType(CycleChangeRequestTypeEnum.ADD_OFFSITE.getRequestType());
        }

        newChangeRequest.setChangeStatusName(ChangeStatusEnum.SAVED.getChangeStatus());

        // TODO: This can be removed since the table column in DB2 has default value
        newChangeRequest.setExpiryTimestamp(DateUtil.getExpiryTimestamp());

        return newChangeRequest;
    }

    public static Boolean validateCycleChangeEffectiveDate(final List<CycleChangeRequest> beforeCycleChanges,
            final List<CycleChangeRequest> afterCycleChanges, CycleChangeRequest submittedCycleChange) {

        boolean isEffDtGood = true;

        List<CycleChangeRequest> descOrderCycleChange = beforeCycleChanges.stream()
                .sorted(Comparator.comparing(CycleChangeRequest::getRunDate)).collect(Collectors.toList());

        List<CycleChangeRequest> ascOrderCycleChange = afterCycleChanges.stream()
                .sorted(Comparator.comparing(CycleChangeRequest::getRunDate)).collect(Collectors.toList());

        // isEffDtGood = beforeCycleChanges.stream().anyMatch(cycle -> {

        // return false;
        // });

        return isEffDtGood;
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

        schedule.setEffectiveDate(DateUtil.getEffectiveDate(runDate, defEffectiveDate));
        schedule.setEffectiveDayName(DateUtil.getDayName(schedule.getEffectiveDate()));

        schedule.setRunDayName(runDay.getDayName());
        schedule.setRunNumber(sequence.getRunSequence());
        schedule.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
        schedule.setOffsiteIndicator(offsiteInd);
        schedule.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());

        return schedule;
    }
}