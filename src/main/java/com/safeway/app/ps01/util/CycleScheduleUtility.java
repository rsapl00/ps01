package com.safeway.app.ps01.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.domain.enums.ChangeStatusEnum;
import com.safeway.app.ps01.domain.enums.CorpEnum;
import com.safeway.app.ps01.domain.enums.CycleChangeRequestTypeEnum;
import com.safeway.app.ps01.domain.enums.DayEnum;
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

    private static CycleChangeRequest createCycleChangeRequest(final CycleSchedule cycleSchedule,
            final LocalDate currentDateInLoop, final DayEnum runDay, final DayEnum defEffectiveDate,
            final String offsiteInd, RunSequenceEnum sequence) {

        CycleChangeRequest schedule = new CycleChangeRequest();

        schedule.setCorpId(CorpEnum.DEFAULT_CORP.getCorpId());
        schedule.setDivId(cycleSchedule.getDivId());
        schedule.setRunDate(java.sql.Date.valueOf(currentDateInLoop));

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        schedule.setCreateTimestamp(ts);

        schedule.setEffectiveDate(DateUtil.getEffectiveDate(currentDateInLoop, defEffectiveDate));

        schedule.setRequestDayName(runDay.getDayName());
        schedule.setRunNumber(sequence.getRunSequence());
        schedule.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
        schedule.setOffsiteIndicator(offsiteInd);
        schedule.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());

        return schedule;
    }
}