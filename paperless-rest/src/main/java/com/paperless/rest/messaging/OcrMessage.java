package com.paperless.rest.messaging;

import java.time.OffsetDateTime;

/**
 * Message sent to the OCR queue after a document upload.
 * Contains only a reference to the document, not the file content itself.
 */
public record OcrMessage(Long documentId, String originalFilename, String contentType, OffsetDateTime uploadedAt) {
}
