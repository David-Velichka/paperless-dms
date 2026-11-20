package com.paperless.rest.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection settings of the RustFS object storage (prefix paperless.rustfs).
 * The application does not start if one of them is blank.
 */
@ConfigurationProperties(prefix = "paperless.rustfs")
@Validated
public record RustFsProperties(
        @NotBlank String endpoint,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String bucket) {
}
