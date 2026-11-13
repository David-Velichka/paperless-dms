package com.paperless.rest.service;

import com.paperless.rest.service.model.DocumentModel;

/**
 * Publishes OCR jobs for uploaded documents.
 * The business layer only knows this interface, the message broker is a detail of the implementation.
 */
public interface OcrJobPublisher {
    void sendOcrMessage(DocumentModel document);
}
