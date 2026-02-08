package com.flashsale.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeAware> {

    @Override
    public boolean isValid(DateRangeAware request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            return true;
        }

        return request.getEndTime().isAfter(request.getStartTime());
    }
}
