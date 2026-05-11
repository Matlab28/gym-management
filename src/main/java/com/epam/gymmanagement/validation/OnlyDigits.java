package com.epam.gymmanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OnlyDigitsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyDigits {

    String message() default "Only digits (0-9) are allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}