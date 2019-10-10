package com.safeway.app.ps01.validation;

import java.sql.Date;
import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RundateValidator implements ConstraintValidator<RundateConstraint, Date> {

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        
        LocalDate date = value.toLocalDate();
        LocalDate currentDate = LocalDate.now();

        if (date.isAfter(currentDate.plusDays(7))) {
            return true;
        }

        return false;
    }


}
