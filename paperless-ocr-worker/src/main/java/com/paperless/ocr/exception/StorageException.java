package com.paperless.ocr.exception;

/**
 * The object storage could not be used (for example it is not reachable). A retry may help.
 */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
