package com.paperless.ocr.exception;

/**
 * Text recognition failed (Ghostscript, Tesseract or temp files). A retry may help.
 */
public class OcrException extends RuntimeException {
    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
