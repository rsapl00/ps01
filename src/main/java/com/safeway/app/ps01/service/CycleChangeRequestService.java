package com.safeway.app.ps01.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.exception.CycleChangeNotFoundException;
import com.safeway.app.ps01.repository.CycleChangeRequestRepository;
import com.safeway.app.ps01.repository.CycleScheduleRepository;
import com.safeway.app.ps01.util.CycleScheduleUtility;

import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CycleChangeRequestService {

    private CycleChangeRequestRepository cycChangeReqRepository;
    private CycleScheduleRepository cycleScheduleRepository;

    public CycleChangeRequestService(CycleChangeRequestRepository repository,
            CycleScheduleRepository cycleScheduleRepository) {
        this.cycChangeReqRepository = repository;
        this.cycleScheduleRepository = cycleScheduleRepository;
    }

    /**
     * 
     * @param divId
     * @param startRunDate
     * @param endRunDate
     * @return
     */
    public List<CycleChangeRequest> findCycleChangeRequestByDivIdAndRunDate(String divId, Date startRunDate,
            Date endRunDate) {
        return cycChangeReqRepository.findByDivIdAndRunDateBetweenOrderByRunDateAsc(divId, startRunDate, endRunDate);
    }

    /**
     * 
     * @param divId
     * @param startRunDate
     * @param endRunDate
     * @return
     */
    @Transactional(readOnly = false)
    public List<CycleChangeRequest> generateCycleChangeRequest(String divId, Date startRunDate, Date endRunDate) {

        // retrieve cycle change requests by division and run date range.
        final List<CycleChangeRequest> cycleChangeRequests = findCycleChangeRequestByDivIdAndRunDate(divId,
                startRunDate, endRunDate);

        final List<CycleChangeRequest> newCycleChangeRequests = generateCycleChangeRequestBasedOnCycleSchedule(divId,
                startRunDate, endRunDate, cycleChangeRequests);

        saveCycleChangeRequests(newCycleChangeRequests);

        return findCycleChangeRequestByDivIdAndRunDate(divId, startRunDate, endRunDate);
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> saveCycleChangeRequests(List<CycleChangeRequest> cycleChangeRequests) {
        return cycChangeReqRepository.saveAll(cycleChangeRequests);
    }

    /**
     * Generate Cycle Change Request base on the given date range. Skip generation
     * if date is already exists in the database.
     * 
     */
    private List<CycleChangeRequest> generateCycleChangeRequestBasedOnCycleSchedule(final String divId,
            final Date startRunDate, final Date endRunDate, final List<CycleChangeRequest> cycleChangeRequests) {
        final List<CycleSchedule> defaultCycleSchedules = cycleScheduleRepository.findByDivIdOrderByDayNumAsc(divId);

        final List<CycleChangeRequest> newSchedules = new ArrayList<>();

        // Set the end date to end date + 1 so that the end date will be included in the
        // generation.
        final LocalDate afterEndRunDate = (endRunDate.toLocalDate()).plusDays(1);

        for (LocalDate date = startRunDate.toLocalDate(); date.isBefore(afterEndRunDate); date = date.plusDays(1)) {

            final LocalDate currentDateInLoop = date;

            // Generate if the current date is not yet in Cycle Change Request
            if (!CycleScheduleUtility.isRunDateExists(currentDateInLoop, cycleChangeRequests)) {
                defaultCycleSchedules.stream().forEach(cycleSchedule -> {
                    newSchedules
                            .addAll(CycleScheduleUtility.generateCycleChangeRequest(cycleSchedule, currentDateInLoop));
                });
            }
        }

        return newSchedules;
    }

	public CycleChangeRequest findById(Long id) {

        Optional<CycleChangeRequest> optional = cycChangeReqRepository.findById(id);

        if (!optional.isPresent()) {
            // TODO: messaging template
            throw new CycleChangeNotFoundException("Cycle Change Request does not exist with ID " + id);
        }

        return optional.get();
	}

	public List<CycleChangeRequest> findAll() {
        return cycChangeReqRepository.findAll();
	}

}