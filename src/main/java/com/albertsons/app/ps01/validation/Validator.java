package com.albertsons.app.ps01.validation;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;

public abstract class Validator {

    protected CycleChangeRequestRepository repository;
    protected Validator validator;

    public Validator(CycleChangeRequestRepository repository) {
        this.repository = repository;
    }

    public Validator(CycleChangeRequestRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public abstract Boolean isValid(final CycleChangeRequestDTO dto);
}