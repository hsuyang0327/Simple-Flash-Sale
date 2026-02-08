package com.flashsale.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateRangeValidator.class)//action into DateRangeValidator
@Target({ElementType.TYPE}) //target
@Retention(RetentionPolicy.RUNTIME) //when code is running
public @interface ValidDateRange {
    String message() default "END_TIME_INVALID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
