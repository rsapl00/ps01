package com.albertsons.app.ps01.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.albertsons.app.ps01.controller.resource.CycleChangeRequestDTO;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;

public class CycleChangeRequestValidator
        implements ConstraintValidator<CycleChangeRequestConstraint, CycleChangeRequestDTO> {

    private CycleChangeRequestRepository repository;

    public CycleChangeRequestValidator(CycleChangeRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize(CycleChangeRequestConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(final CycleChangeRequestDTO cycleChangeDTO, ConstraintValidatorContext context) {
        try {

            Validator offsiteValidator = new CycleChangeOffsiteValidator(repository);
            Validator sequenceValidator = new CycleChangeRunSequenceValidator(repository, offsiteValidator);
            Validator effDateValidator = new CycleChangeEffectiveDateValidator(repository, sequenceValidator);
            Validator runDateValidator = new CycleChangeRunDateValidator(repository, effDateValidator);

            return runDateValidator.isValid(cycleChangeDTO);
           
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();

            return false;
        }
    }
}