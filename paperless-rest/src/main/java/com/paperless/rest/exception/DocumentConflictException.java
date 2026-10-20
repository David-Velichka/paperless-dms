package com.paperless.rest.exception;

public class DocumentConflictException extends RuntimeException {
    public DocumentConflictException(String message) {
        super(message);
    }

    public DocumentConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

