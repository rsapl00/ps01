package com.safeway.app.ps01.validation;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;

public class CycleChangeSearchValidator implements ConstraintValidator<CycleChangeSearchConstraint, Object> {

    private String startDate;
    private String endDate;

    @Override
    public void initialize(CycleChangeSearchConstraint constraintAnnotation) {
        this.startDate = constraintAnnotation.startDate();
        this.endDate = constraintAnnotation.endDate();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object start = new BeanWrapperImpl(value).getPropertyValue(startDate);
        final LocalDate startDate = LocalDate.parse((CharSequence) start.toString());

        Object end = new BeanWrapperImpl(value).getPropertyValue(endDate);
        LocalDate endDate = LocalDate.parse((CharSequence) end.toString());

        return startDate.isBefore(endDate);
    }
}