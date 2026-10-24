package com.paperless.ocr.messaging;

import java.time.OffsetDateTime;

/**
 * OCR job received from paperless-rest after a document upload.
 */
public record OcrMessage(Long documentId, String originalFilename, String contentType, OffsetDateTime uploadedAt) {
}
