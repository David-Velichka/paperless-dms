package com.paperless.rest.service.validation;

import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorFactoryTest {

    // GIVEN die ValidatorFactory und ein gültiges DocumentModel
    // WHEN forClass(DocumentModel.class).validate aufgerufen wird
    // THEN liefert der Validator ein leeres ConstraintViolation-Set
    @Test
    void forClass_WithValidDocumentModel_ReturnsNoViolations() {
        Validator<DocumentModel> validator = ValidatorFactory.forClass(DocumentModel.class);
        DocumentModel model = DocumentModel.builder()
                .title("Valid Doc")
                .originalFilename("valid.pdf")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        Set<ConstraintViolation<DocumentModel>> violations = validator.validate(model);
        assertTrue(violations.isEmpty());
    }

    // GIVEN die ValidatorFactory und ein ungültiges DocumentModel mit leerem Titel
    // WHEN forClass(DocumentModel.class).validate aufgerufen wird
    // THEN liefert der Validator ConstraintViolations mit entsprechender Fehlermeldung
    @Test
    void forClass_WithInvalidDocumentModel_ReturnsViolations() {
        Validator<DocumentModel> validator = ValidatorFactory.forClass(DocumentModel.class);
        DocumentModel model = DocumentModel.builder()
                .title("   ")
                .originalFilename("valid.pdf")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        Set<ConstraintViolation<DocumentModel>> violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Document title must not be blank")));
    }
}
