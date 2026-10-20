package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStatusHistoryModelValidatorTest {

    private DocumentStatusHistoryModelValidator historyModelValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        historyModelValidator = new DocumentStatusHistoryModelValidator(validator);
    }

    // GIVEN ein vollständig gültiges DocumentStatusHistoryModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird keine Exception geworfen und die Validierung ist erfolgreich
    @Test
    void validateOrThrow_WithValidModel_Succeeds() {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .documentId(1L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .timestamp(OffsetDateTime.now())
                .comment("OCR worker started")
                .build();

        assertDoesNotThrow(() -> historyModelValidator.validateOrThrow(model));
        assertTrue(historyModelValidator.validate(model).isEmpty());
    }

    // GIVEN ein Null-DocumentStatusHistoryModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithNullModel_ThrowsBusinessLayerException() {
        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> historyModelValidator.validateOrThrow(null));
        assertEquals("Document status history model must not be null", ex.getMessage());
    }

    // GIVEN ein DocumentStatusHistoryModel ohne Dokumenten-ID
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithNullDocumentId_ThrowsBusinessLayerException() {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .documentId(null)
                .status(DocumentStatus.PROCESSING)
                .timestamp(OffsetDateTime.now())
                .build();

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> historyModelValidator.validateOrThrow(model));
        assertTrue(ex.getMessage().contains("Document id must not be null"));
    }

    // GIVEN ein DocumentStatusHistoryModel mit einem Kommentar über 500 Zeichen
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithExcessiveComment_ThrowsBusinessLayerException() {
        String longComment = "a".repeat(501);
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .documentId(1L)
                .status(DocumentStatus.PROCESSING)
                .timestamp(OffsetDateTime.now())
                .comment(longComment)
                .build();

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> historyModelValidator.validateOrThrow(model));
        assertTrue(ex.getMessage().contains("Comment must not exceed 500 characters"));
    }
}
