package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentModelValidatorTest {

    private DocumentModelValidator documentModelValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        documentModelValidator = new DocumentModelValidator(validator);
    }

    // GIVEN ein vollständig gültiges DocumentModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird keine Exception geworfen und die Validierung ist erfolgreich
    @Test
    void validateOrThrow_WithValidModel_Succeeds() {
        DocumentModel model = DocumentModel.builder()
                .title("Valid Title")
                .originalFilename("invoice_2026.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        assertDoesNotThrow(() -> documentModelValidator.validateOrThrow(model));
        assertTrue(documentModelValidator.validate(model).isEmpty());
    }

    // GIVEN ein Null-DocumentModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithNullModel_ThrowsBusinessLayerException() {
        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> documentModelValidator.validateOrThrow(null));
        assertEquals("Document model must not be null", ex.getMessage());
    }

    // GIVEN ein DocumentModel mit leerem Titel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException mit Fehlermeldung geworfen
    @Test
    void validateOrThrow_WithBlankTitle_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder()
                .title("   ")
                .originalFilename("sample.pdf")
                .build();

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> documentModelValidator.validateOrThrow(model));
        assertTrue(ex.getMessage().contains("Document title must not be blank"));
    }

    // GIVEN ein DocumentModel mit negativem Dateigewicht
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException mit Fehlermeldung geworfen
    @Test
    void validateOrThrow_WithNegativeFileSize_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder()
                .title("Valid Title")
                .fileSize(-50L)
                .build();

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> documentModelValidator.validateOrThrow(model));
        assertTrue(ex.getMessage().contains("File size must not be negative"));
    }

    // GIVEN ein Dateiname mit Path-Traversal-Sequenzen wie Punkt-Punkt-Slash
    // WHEN validateOrThrow aufgerufen wird
    // THEN schlägt die benutzerdefinierte Validierung fehl und wirft eine BusinessLayerException
    @Test
    void validateOrThrow_WithPathTraversalFilename_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder()
                .title("Valid Title")
                .originalFilename("../../etc/passwd")
                .build();

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> documentModelValidator.validateOrThrow(model));
        assertTrue(ex.getMessage().contains("Filename must not contain path traversal sequences or illegal characters"));
    }
}

