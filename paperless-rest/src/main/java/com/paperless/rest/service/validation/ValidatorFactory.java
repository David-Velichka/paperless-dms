package com.paperless.rest.service.validation;

import jakarta.validation.Validation;

/**
 * Provides programmatic validators for arbitrary classes.
 */
public class ValidatorFactory {

    /**
     * Builds a Validator instance for the specified class.
     *
     * @param clazz class type to validate
     * @param <T> generic entity type
     * @return Validator instance
     */
    public static <T> Validator<T> forClass(Class<T> clazz) {
        return obj -> Validation.buildDefaultValidatorFactory()
                .getValidator()
                .validate(obj);
    }
}

