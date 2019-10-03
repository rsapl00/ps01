package com.safeway.app.ps01.controller;

import java.sql.Date;
import java.util.List;

import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.service.CycleChangeRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CycleChangeRequestController {

    private CycleChangeRequestService cycleChangeRequestService;

    public CycleChangeRequestController(CycleChangeRequestService cycleChangeRequestService) {
        this.cycleChangeRequestService = cycleChangeRequestService;
    }

    @GetMapping("/cyclechange")
    public ResponseEntity<List<CycleChangeRequest>> getCycleChangeRequests() {

        return null;
    }

    @GetMapping("/cyclechange/{divisionId}")
    public ResponseEntity<List<CycleChangeRequest>> getCycleChangeRequestByDivision(@PathVariable String divisionId) {

        return null;
    }

    @GetMapping("/cyclechange/{divisionId}/{startRunDate}/{endRunDate}")
    public ResponseEntity<CycleChangeRequest> getCycleChangeRequestByDateRange(@PathVariable String divisionId,
            @PathVariable Date startRunDate, @PathVariable Date endRunDate) {

        return null;
    }

}