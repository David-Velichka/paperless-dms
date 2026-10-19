package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.DocumentModel;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Business validator for {@link DocumentModel}.
 * Enforces declarative bean validation constraints and throws layer-specific exceptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentModelValidator implements Validator<DocumentModel> {

    private final jakarta.validation.Validator validator;

    @Override
    public Set<ConstraintViolation<DocumentModel>> validate(DocumentModel obj) {
        if (obj == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        return validator.validate(obj);
    }

    /**
     * Validates the document model and throws {@link BusinessLayerException} on the first constraint violation.
     *
     * @param obj document model to validate
     */
    public void validateOrThrow(DocumentModel obj) {
        Set<ConstraintViolation<DocumentModel>> violations = validate(obj);
        if (!violations.isEmpty()) {
            ConstraintViolation<DocumentModel> violation = violations.iterator().next();
            log.warn("Validation failure on DocumentModel: {} - {}", violation.getPropertyPath(), violation.getMessage());
            throw new BusinessLayerException("Validation failed for Document: " + violation.getMessage());
        }
    }
}

