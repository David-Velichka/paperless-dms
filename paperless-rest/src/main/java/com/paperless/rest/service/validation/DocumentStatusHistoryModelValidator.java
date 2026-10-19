package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Business validator for {@link DocumentStatusHistoryModel}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusHistoryModelValidator implements Validator<DocumentStatusHistoryModel> {

    private final jakarta.validation.Validator validator;

    @Override
    public Set<ConstraintViolation<DocumentStatusHistoryModel>> validate(DocumentStatusHistoryModel obj) {
        if (obj == null) {
            throw new BusinessLayerException("Document status history model must not be null");
        }
        return validator.validate(obj);
    }

    /**
     * Validates the status history model and throws {@link BusinessLayerException} on the first constraint violation.
     *
     * @param obj history model to validate
     */
    public void validateOrThrow(DocumentStatusHistoryModel obj) {
        Set<ConstraintViolation<DocumentStatusHistoryModel>> violations = validate(obj);
        if (!violations.isEmpty()) {
            ConstraintViolation<DocumentStatusHistoryModel> violation = violations.iterator().next();
            log.warn("Validation failure on DocumentStatusHistoryModel: {} - {}", violation.getPropertyPath(), violation.getMessage());
            throw new BusinessLayerException("Validation failed for StatusHistory: " + violation.getMessage());
        }
    }
}

