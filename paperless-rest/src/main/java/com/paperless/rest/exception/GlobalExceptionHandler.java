package com.paperless.rest.exception;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFound(DocumentNotFoundException ex) {
        log.warn("Document not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Document Not Found");
        problem.setType(URI.create("urn:paperless:problem:document-not-found"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ProblemDetail handleInvalidDocument(InvalidDocumentException ex) {
        log.warn("Invalid document payload: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Document");
        problem.setType(URI.create("urn:paperless:problem:invalid-document"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidStatusTransitionException ex) {
        log.warn("Invalid lifecycle transition: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid Status Transition");
        problem.setType(URI.create("urn:paperless:problem:invalid-status-transition"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(DocumentConflictException.class)
    public ProblemDetail handleDocumentConflict(DocumentConflictException ex) {
        log.warn("Document conflict: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Document Conflict");
        problem.setType(URI.create("urn:paperless:problem:document-conflict"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(BusinessLayerException.class)
    public ProblemDetail handleBusinessLayerException(BusinessLayerException ex) {
        log.warn("Business layer exception occurred: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("urn:paperless:problem:business-layer-error"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(MessageQueueException.class)
    public ProblemDetail handleMessageQueueException(MessageQueueException ex) {
        log.error("Message queue unavailable: {}", ex.getMessage());
        String message = "The document could not be queued for processing. Please try again later.";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, message);
        problem.setTitle("Message Queue Unavailable");
        problem.setType(URI.create("urn:paperless:problem:message-queue-unavailable"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", message);
        return problem;
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ProblemDetail handleOptimisticLock(Exception ex) {
        log.warn("Concurrent update conflict detected: {}", ex.getMessage());
        String message = "The document was modified concurrently by another transaction. Please refresh and try again.";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problem.setTitle("Conflict");
        problem.setType(URI.create("urn:paperless:problem:optimistic-lock-conflict"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", message);
        return problem;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, org.springframework.validation.BindException.class})
    public ProblemDetail handleValidationExceptions(org.springframework.validation.BindException ex) {
        log.warn("Input validation failure: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more request parameters");
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("urn:paperless:problem:validation-error"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", fieldErrors);
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled internal server exception", ex);
        String message = "An unexpected error occurred. Please try again later.";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, message);
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("urn:paperless:problem:internal-server-error"));
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("error", problem.getTitle());
        problem.setProperty("message", message);
        return problem;
    }
}
