package com.paperless.ocr.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Names of the RabbitMQ exchange, queues and routing key used by the worker (prefix paperless.rabbitmq).
 * The application does not start if one of them is blank.
 */
@ConfigurationProperties(prefix = "paperless.rabbitmq")
@Validated
public record RabbitMqProperties(
        @NotBlank String exchange,
        @NotBlank String ocrQueue,
        @NotBlank String ocrResultQueue,
        @NotBlank String ocrResultRoutingKey) {
}
