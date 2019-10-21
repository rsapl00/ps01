package com.albertsons.app.ps01.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.repository.CycleScheduleRepository;
import com.albertsons.app.ps01.security.userdetails.RoleType;
import com.albertsons.app.ps01.security.userdetails.User;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CycleScheduleService {

    final private CycleChangeRequestService cycleChangeRequestService;
    final private CycleScheduleRepository cycleScheduleRepository;

    protected CycleScheduleService(CycleChangeRequestService cycleChangeRequestService, CycleScheduleRepository repository) {
        this.cycleChangeRequestService = cycleChangeRequestService;
        this.cycleScheduleRepository = repository;
    }

    public List<CycleChangeRequest> findBaseCycleSchedule(final String divisionId, final Date startDate, final Date endDate) {

        return cycleChangeRequestService.generateCycleChangeRequestBasedOnCycleSchedule(divisionId, startDate, endDate,
                new ArrayList<>());

    }

    public List<String> findDistinctDivision() {
        
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getRole() == RoleType.USER_RIM) {
            return Arrays.asList(cycleScheduleRepository.findDistinctDivIdByDivId(user.getDivision()));
        } else {
            return cycleScheduleRepository.findAllDistinctDivId();
        }

    }

}