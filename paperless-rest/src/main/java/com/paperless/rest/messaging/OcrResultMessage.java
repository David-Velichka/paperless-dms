package com.paperless.rest.messaging;

/**
 * Result event received from the OCR worker. The OCR text is only set for COMPLETED.
 */
public record OcrResultMessage(
        Long documentId,
        OcrResultStatus status,
        String ocrText) {
}
