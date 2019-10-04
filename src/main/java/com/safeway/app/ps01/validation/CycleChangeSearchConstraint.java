package com.safeway.app.ps01.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = CycleChangeSearchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CycleChangeSearchConstraint {

    String message() default "Start date should not be later than end date.";

    String startDate();

    String endDate();

    @Target( {ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CycleChangeSearchConstraint[] value();
    }

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {}; 
}