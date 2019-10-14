package com.albertsons.app.ps01.controller.resource.assembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import com.albertsons.app.ps01.controller.CycleScheduleController;
import com.albertsons.app.ps01.domain.CycleChangeRequest;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class CycleScheduleResourceAssembler
        implements ResourceAssembler<CycleChangeRequest, Resource<CycleChangeRequest>> {

    @Override
    public Resource<CycleChangeRequest> toResource(CycleChangeRequest baseSchedule) {
        return new Resource<>(baseSchedule,
                linkTo(methodOn(CycleScheduleController.class).getBaseCycleSchedules(baseSchedule.getDivId(),
                        baseSchedule.getRunDate(), baseSchedule.getRunDate())).withSelfRel());
    }

}