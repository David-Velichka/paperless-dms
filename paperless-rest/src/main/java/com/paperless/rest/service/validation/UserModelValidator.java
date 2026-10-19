package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.UserModel;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Business validator for {@link UserModel}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserModelValidator implements Validator<UserModel> {

    private final jakarta.validation.Validator validator;

    @Override
    public Set<ConstraintViolation<UserModel>> validate(UserModel obj) {
        if (obj == null) {
            throw new BusinessLayerException("User model must not be null");
        }
        return validator.validate(obj);
    }

    /**
     * Validates the user model and throws {@link BusinessLayerException} on the first constraint violation.
     *
     * @param obj user model to validate
     */
    public void validateOrThrow(UserModel obj) {
        Set<ConstraintViolation<UserModel>> violations = validate(obj);
        if (!violations.isEmpty()) {
            ConstraintViolation<UserModel> violation = violations.iterator().next();
            log.warn("Validation failure on UserModel: {} - {}", violation.getPropertyPath(), violation.getMessage());
            throw new BusinessLayerException("Validation failed for User: " + violation.getMessage());
        }
    }
}

