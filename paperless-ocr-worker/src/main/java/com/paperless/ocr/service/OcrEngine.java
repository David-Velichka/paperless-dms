package com.paperless.ocr.service;

import java.nio.file.Path;

/**
 * Recognizes the text of a PDF file.
 */
public interface OcrEngine {

    /** Returns the text of all pages. A PDF without recognizable text gives an empty string. */
    String extractText(Path pdf);
}
