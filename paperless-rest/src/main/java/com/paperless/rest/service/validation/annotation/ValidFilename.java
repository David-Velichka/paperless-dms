package com.paperless.rest.service.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation matching the pattern demonstrated in course Slide 18.
 * Ensures filenames do not contain path traversal sequences or illegal control characters.
 */
@Documented
@Constraint(validatedBy = ValidFilenameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilename {

    String message() default "Filename must not contain path traversal sequences or illegal characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

