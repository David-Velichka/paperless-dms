package com.paperless.ocr.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Connection settings of the RustFS object storage (prefix paperless.rustfs).
 * apiCallTimeout limits a whole call including retries, apiCallAttemptTimeout a single attempt.
 * The application does not start if one of them is blank or missing.
 */
@ConfigurationProperties(prefix = "paperless.rustfs")
@Validated
public record RustFsProperties(
        @NotBlank String endpoint,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String bucket,
        @NotNull Duration apiCallTimeout,
        @NotNull Duration apiCallAttemptTimeout) {
}
