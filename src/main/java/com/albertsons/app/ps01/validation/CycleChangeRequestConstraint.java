package com.albertsons.app.ps01.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = CycleChangeRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CycleChangeRequestConstraint {

    String message() default "Invalid Cycle Change Request.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {}; 
}