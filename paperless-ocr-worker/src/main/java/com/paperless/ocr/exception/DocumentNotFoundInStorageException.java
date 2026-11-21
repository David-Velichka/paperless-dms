package com.paperless.ocr.exception;

/**
 * The object does not exist in the storage, for example because the document was deleted meanwhile.
 * A retry cannot succeed.
 */
public class DocumentNotFoundInStorageException extends RuntimeException {
    public DocumentNotFoundInStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
