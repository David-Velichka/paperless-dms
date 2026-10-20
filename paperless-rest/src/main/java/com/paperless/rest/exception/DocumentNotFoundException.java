package com.paperless.rest.exception;

public class DocumentNotFoundException extends BusinessLayerException {
    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

