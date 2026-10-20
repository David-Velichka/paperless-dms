package com.paperless.rest.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // GIVEN eine DocumentNotFoundException mit einer spezifischen Fehlermeldung
    // WHEN die handleDocumentNotFound-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 404 zurückgegeben
    @Test
    void handleDocumentNotFound_ReturnsProblemDetail404() {
        DocumentNotFoundException ex = new DocumentNotFoundException("Document 123 not found");

        ProblemDetail problem = handler.handleDocumentNotFound(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Document Not Found", problem.getTitle());
        assertEquals("Document 123 not found", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:document-not-found"), problem.getType());
        assertNotNull(problem.getProperties());
        assertNotNull(problem.getProperties().get("timestamp"));
        assertEquals("Document Not Found", problem.getProperties().get("error"));
        assertEquals("Document 123 not found", problem.getProperties().get("message"));
    }

    // GIVEN eine InvalidDocumentException mit einer spezifischen Fehlermeldung
    // WHEN die handleInvalidDocument-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 400 zurückgegeben
    @Test
    void handleInvalidDocument_ReturnsProblemDetail400() {
        InvalidDocumentException ex = new InvalidDocumentException("File payload is empty");

        ProblemDetail problem = handler.handleInvalidDocument(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Invalid Document", problem.getTitle());
        assertEquals("File payload is empty", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:invalid-document"), problem.getType());
    }

    // GIVEN eine InvalidStatusTransitionException bei einem unzulässigen Statuswechsel
    // WHEN die handleInvalidTransition-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 422 zurückgegeben
    @Test
    void handleInvalidTransition_ReturnsProblemDetail422() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("Cannot move from RECEIVED to COMPLETED");

        ProblemDetail problem = handler.handleInvalidTransition(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), problem.getStatus());
        assertEquals("Invalid Status Transition", problem.getTitle());
        assertEquals("Cannot move from RECEIVED to COMPLETED", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:invalid-status-transition"), problem.getType());
    }

    // GIVEN eine DocumentConflictException bei einem Versionskonflikt
    // WHEN die handleDocumentConflict-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 409 zurückgegeben
    @Test
    void handleDocumentConflict_ReturnsProblemDetail409() {
        DocumentConflictException ex = new DocumentConflictException("Conflict detected on document");

        ProblemDetail problem = handler.handleDocumentConflict(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Document Conflict", problem.getTitle());
        assertEquals("Conflict detected on document", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:document-conflict"), problem.getType());
    }

    // GIVEN eine InvalidCredentialsException bei fehlerhafter Authentifizierung
    // WHEN die handleInvalidCredentials-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 401 zurückgegeben
    @Test
    void handleInvalidCredentials_ReturnsProblemDetail401() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Bad username or password");

        ProblemDetail problem = handler.handleInvalidCredentials(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("Unauthorized", problem.getTitle());
        assertEquals("Bad username or password", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:unauthorized"), problem.getType());
    }

    // GIVEN eine BusinessLayerException bei einem allgemeinen Geschäftslogikfehler
    // WHEN die handleBusinessLayerException-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 400 zurückgegeben
    @Test
    void handleBusinessLayerException_ReturnsProblemDetail400() {
        BusinessLayerException ex = new BusinessLayerException("Operation failed");

        ProblemDetail problem = handler.handleBusinessLayerException(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Bad Request", problem.getTitle());
        assertEquals("Operation failed", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:business-layer-error"), problem.getType());
    }

    // GIVEN eine OptimisticLockException bei nebenläufiger Bearbeitung
    // WHEN die handleOptimisticLock-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 409 zurückgegeben
    @Test
    void handleOptimisticLock_ReturnsProblemDetail409() {
        OptimisticLockException ex = new OptimisticLockException("Row lock failed");

        ProblemDetail problem = handler.handleOptimisticLock(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Conflict", problem.getTitle());
        assertEquals("The document was modified concurrently by another transaction. Please refresh and try again.", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:optimistic-lock-conflict"), problem.getType());
    }

    // GIVEN ein Validierungsfehler mit BindingResult Feldern
    // WHEN die handleValidationExceptions-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit Fehlerdetails und HTTP 400 zurückgegeben
    @Test
    @SuppressWarnings("unchecked")
    void handleValidationExceptions_ReturnsProblemDetailWithInvalidParams() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("documentDto", "title", "Title is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problem = handler.handleValidationExceptions(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
        assertNotNull(problem.getProperties());
        Map<String, String> invalidParams = (Map<String, String>) problem.getProperties().get("invalidParams");
        assertNotNull(invalidParams);
        assertEquals("Title is required", invalidParams.get("title"));
    }

    // GIVEN eine unerwartete generische Ausnahme
    // WHEN die handleGenericException-Methode aufgerufen wird
    // THEN wird ein RFC 7807 ProblemDetail mit HTTP-Status 500 zurückgegeben
    @Test
    void handleGenericException_ReturnsProblemDetail500() {
        RuntimeException ex = new RuntimeException("Unexpected disk failure");

        ProblemDetail problem = handler.handleGenericException(ex);

        assertNotNull(problem);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Internal Server Error", problem.getTitle());
        assertEquals("An unexpected error occurred. Please try again later.", problem.getDetail());
        assertEquals(URI.create("urn:paperless:problem:internal-server-error"), problem.getType());
    }
}

