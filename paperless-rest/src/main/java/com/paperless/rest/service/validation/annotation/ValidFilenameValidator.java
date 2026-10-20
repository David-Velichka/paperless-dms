package com.paperless.rest.service.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidFilename}.
 */
public class ValidFilenameValidator implements ConstraintValidator<ValidFilename, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Blank / null is handled by @NotBlank or @NotNull if desired
        }
        // Disallow path traversal patterns and null bytes
        if (value.contains("..") || value.contains("/") || value.contains("\\") || value.contains("\0")) {
            return false;
        }
        return true;
    }
}

