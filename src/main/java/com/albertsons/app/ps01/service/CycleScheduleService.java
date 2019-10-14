package com.albertsons.app.ps01.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.albertsons.app.ps01.domain.CycleChangeRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CycleScheduleService {

    private CycleChangeRequestService cycleChangeRequestService;

    protected CycleScheduleService(CycleChangeRequestService cycleChangeRequestService) {
        this.cycleChangeRequestService = cycleChangeRequestService;
    }

    public List<CycleChangeRequest> findBaseCycleSchedule(String divisionId, Date startDate, Date endDate) {

        return cycleChangeRequestService.generateCycleChangeRequestBasedOnCycleSchedule(divisionId, startDate, endDate,
                new ArrayList<>());

    }

}