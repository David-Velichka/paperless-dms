package com.paperless.ocr.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Tesseract settings (prefix paperless.ocr): folder with the language files and the languages to recognize.
 * The application does not start if one of them is blank.
 */
@ConfigurationProperties(prefix = "paperless.ocr")
@Validated
public record OcrProperties(
        @NotBlank String tessdataPath,
        @NotBlank String language) {
}
