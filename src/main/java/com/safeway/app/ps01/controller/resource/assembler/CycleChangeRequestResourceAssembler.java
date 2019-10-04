package com.safeway.app.ps01.controller.resource.assembler;

import com.safeway.app.ps01.controller.CycleChangeRequestController;
import com.safeway.app.ps01.domain.CycleChangeRequest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class CycleChangeRequestResourceAssembler
        implements ResourceAssembler<CycleChangeRequest, Resource<CycleChangeRequest>> {

    @Override
    public Resource<CycleChangeRequest> toResource(CycleChangeRequest cycleChangeRequest) {
    
        return new Resource<>(cycleChangeRequest,
                linkTo(methodOn(CycleChangeRequestController.class).getCycleChangeById(cycleChangeRequest.getId())).withSelfRel());

    }

}