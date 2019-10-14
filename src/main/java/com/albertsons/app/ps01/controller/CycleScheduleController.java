package com.albertsons.app.ps01.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.albertsons.app.ps01.controller.resource.assembler.CycleScheduleResourceAssembler;
import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.service.CycleScheduleService;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CycleScheduleController {

    private CycleScheduleService cycleScheduleService;
    private CycleScheduleResourceAssembler assembler;

    public CycleScheduleController(CycleScheduleService cycleScheduleService, CycleScheduleResourceAssembler assembler) {
        this.cycleScheduleService = cycleScheduleService;
        this.assembler = assembler;
    }

    @GetMapping("/cycleschedules/{divisionId}")
    public Resources<Resource<CycleChangeRequest>> getBaseCycleSchedules(@PathVariable String divisionId,
            @RequestParam Date startDate, @RequestParam Date endDate) {

        List<Resource<CycleChangeRequest>> cycleSchedules = cycleScheduleService
                .findBaseCycleSchedule(divisionId, startDate, endDate).stream().map(assembler::toResource)
                .collect(Collectors.toList());

        return new Resources<>(cycleSchedules,
                linkTo(methodOn(CycleScheduleController.class).getBaseCycleSchedules(divisionId, startDate, endDate))
                        .withSelfRel());
    }
}