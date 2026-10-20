package com.paperless.rest.service.validation;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Generic validator interface as demonstrated in course materials (Self-Study G / Slide 21).
 *
 * @param <T> the type of entity/model to validate
 */
public interface Validator<T> {

    /**
     * Validates an object and returns any constraint violations.
     *
     * @param obj the object to validate
     * @return set of constraint violations (empty if valid)
     */
    Set<ConstraintViolation<T>> validate(T obj);
}

