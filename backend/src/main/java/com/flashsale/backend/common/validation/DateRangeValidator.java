package com.flashsale.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @description Validator for event date range — ensures startTime is before endTime
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeAware> {

    @Override
    /**
     * @description Validate that endTime is after startTime
     * @author Yang-Hsu
     * @date 2026/7/9
     */
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
