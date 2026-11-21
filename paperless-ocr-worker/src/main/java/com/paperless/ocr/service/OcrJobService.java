package com.paperless.ocr.service;

import com.paperless.ocr.messaging.OcrMessage;

/**
 * Processes one OCR job: fetches the PDF from the storage and recognizes its text.
 */
public interface OcrJobService {

    void process(OcrMessage message);
}
