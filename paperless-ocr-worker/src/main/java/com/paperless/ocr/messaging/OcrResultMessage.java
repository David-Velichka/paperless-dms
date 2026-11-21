package com.paperless.ocr.messaging;

/**
 * Result event sent back to paperless-rest. The OCR text is only set for COMPLETED.
 */
public record OcrResultMessage(
        Long documentId,
        OcrResultStatus status,
        String ocrText) {
}
