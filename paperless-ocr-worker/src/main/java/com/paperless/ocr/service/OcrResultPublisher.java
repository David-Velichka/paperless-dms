package com.paperless.ocr.service;

import com.paperless.ocr.messaging.OcrResultMessage;

/**
 * Reports the progress and the result of an OCR job back to paperless-rest.
 * The worker has no database access, the REST server updates the document itself.
 */
public interface OcrResultPublisher {

    void publish(OcrResultMessage message);
}
