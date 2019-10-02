package com.safeway.app.ps01.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.repository.CycleChangeRequestRepository;
import com.safeway.app.ps01.repository.CycleScheduleRepository;
import com.safeway.app.ps01.util.CycleScheduleConverter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CycleChangeRequestService {

    private CycleChangeRequestRepository cycChangeReqRepository;
    private CycleScheduleRepository cycleScheduleRepository;

    public CycleChangeRequestService(CycleChangeRequestRepository repository, CycleScheduleRepository cycleScheduleRepository) {
        this.cycChangeReqRepository = repository;
        this.cycleScheduleRepository = cycleScheduleRepository;
    }

    public List<CycleChangeRequest> findCycleChangeRequestByDivIdAndRunDate(String divId, Date startRunDate, Date endRunDate) {
        return cycChangeReqRepository.findByDivIdAndRunDateBetweenOrderByRunDateAsc(divId, startRunDate, endRunDate);
    }

    public List<CycleChangeRequest> generateCycleChangeRequest(String divId, Date startRunDate, Date endRunDate) {

        // retrieve cycle change requests by division and run date range.
        final List<CycleChangeRequest> cycleChangeRequests = findCycleChangeRequestByDivIdAndRunDate(divId, startRunDate, endRunDate);

        // check if there is a current record for cycle change request for the specified start and end run date.
        // if there is no or incomplete record generate one.
        // to generate new records, first get the base schedule of the division from PSCYCSCH table.
        
        // Generate default cycle change request if there's no record yet for the specified run dates.
        if (cycleChangeRequests.isEmpty() || cycleChangeRequests == null) {
            cycleChangeRequests.addAll(mapCycleScheduleToCycleChangeRequest(divId, startRunDate, endRunDate));
        } else {

            // check if all run dates have records, if not, generate missing cycle change requests for the missing dates.
            // loop through start date to end date while comparing it to the cycle change requests records.
            cycleChangeRequests.addAll(generateMissingCycleChangeDates(divId, startRunDate, endRunDate));
        }

        return cycleChangeRequests;
    }

    private List<CycleChangeRequest> generateMissingCycleChangeDates(final  String divId, final Date startRunDate, final Date endRunDate) {
        // final List<CycleSchedule> defaultCycleSchedules = cycleScheduleRepository.findByDivIdOrderByDayNumAsc(divId);

        return null;
        
    }

    private List<CycleChangeRequest> mapCycleScheduleToCycleChangeRequest(final String divId, final Date startRunDate, final Date endRunDate) {
        final List<CycleSchedule> defaultCycleSchedules = cycleScheduleRepository.findByDivIdOrderByDayNumAsc(divId);

        final List<CycleChangeRequest> defaultSchedules = new ArrayList<>();

        for (LocalDate date = startRunDate.toLocalDate(); 
                    date.isBefore(endRunDate.toLocalDate()); 
                    date = date.plusDays(1) ) {

            final LocalDate currentDateInLoop = date;

            defaultCycleSchedules.stream().forEach(cycleSchedule -> {
                defaultSchedules.addAll(CycleScheduleConverter.generateCycleChangeRequest(cycleSchedule, currentDateInLoop));
            });            
        }

        return defaultSchedules;
    }

    

}