package com.safeway.app.ps01.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.safeway.app.ps01.controller.resource.CycleChangeSearchDTO;
import com.safeway.app.ps01.controller.resource.assembler.CycleChangeRequestResourceAssembler;
import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.service.CycleChangeRequestService;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CycleChangeRequestController {

    private CycleChangeRequestService cycleChangeRequestService;
    private CycleChangeRequestResourceAssembler assembler;

    public CycleChangeRequestController(CycleChangeRequestService cycleChangeRequestService,
            CycleChangeRequestResourceAssembler assembler) {
        this.cycleChangeRequestService = cycleChangeRequestService;
        this.assembler = assembler;
    }

    @GetMapping("/cyclechanges")
    public Resources<Resource<CycleChangeRequest>> getAllCycleChangeRequests() {
        List<Resource<CycleChangeRequest>> cycleChanges = cycleChangeRequestService.findAll().stream()
                .map(assembler::toResource).collect(Collectors.toList());

        return new Resources<>(cycleChanges,
                linkTo(methodOn(CycleChangeRequestController.class).getAllCycleChangeRequests()).withSelfRel());
    }

    @PostMapping("/cyclechanges")
    public ResponseEntity<?> submitNewCycleChange(@Valid @RequestBody CycleChangeRequest newCycleChange)
            throws URISyntaxException {

        Resource<CycleChangeRequest> resource = assembler
                .toResource(cycleChangeRequestService.saveCycleChangeRequest(newCycleChange));

        return ResponseEntity.created(new URI(resource.getId().expand().getHref())).body(resource);
    }

    @PostMapping("/cyclechanges/schedules")
    public ResponseEntity<?> getCycleChangeByRunDateFromAndTo(@Valid @RequestBody CycleChangeSearchDTO cycleChange)
            throws URISyntaxException {

        List<Resource<CycleChangeRequest>> cycleChanges = cycleChangeRequestService
                .generateCycleChangeRequest(cycleChange.getDivisionId(), cycleChange.getStartDate(),
                        cycleChange.getEndDate())
                .stream().map(assembler::toResource).collect(Collectors.toList());

        return ResponseEntity.created(new URI(
                linkTo(methodOn(CycleChangeRequestController.class).getCycleChangeByRunDateFromAndTo(cycleChange))
                        .withSelfRel().getHref()))
                .body(new Resources<>(cycleChanges, linkTo(
                        methodOn(CycleChangeRequestController.class).getCycleChangeByRunDateFromAndTo(cycleChange))
                                .withSelfRel()));
    }

    @GetMapping("/cyclechanges/{id}")
    public Resource<CycleChangeRequest> getCycleChangeById(@PathVariable Long id) {

        CycleChangeRequest cycleChange = cycleChangeRequestService.findById(id);

        return new Resource<>(cycleChange,
                linkTo(methodOn(CycleChangeRequestController.class).getCycleChangeById(id)).withSelfRel());
    }

    @PutMapping("/cyclechanges/{id}")
    public ResponseEntity<?> updateCycleChangeRequest(@RequestBody CycleChangeRequest cycleChangeRequest,
            @PathVariable Long id) throws URISyntaxException {

        // TODO: call service
        CycleChangeRequest updatedCycleChange = new CycleChangeRequest();

        Resource<CycleChangeRequest> resource = assembler.toResource(updatedCycleChange);

        return ResponseEntity.created(new URI(resource.getId().expand().getHref())).body(resource);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCycleChangeRequest(@PathVariable Long id) {

        // TODO: call delete service; delete service only put expiration date on the
        // record.

        return ResponseEntity.noContent().build();
    }
}