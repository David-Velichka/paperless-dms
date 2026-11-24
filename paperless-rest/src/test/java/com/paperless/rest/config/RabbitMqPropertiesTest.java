package com.paperless.rest.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMqPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(RabbitMqProperties.class)
    static class PropertiesConfig {
    }

    // GIVEN alle RabbitMQ-Namen sind konfiguriert
    // WHEN der Kontext gestartet wird
    // THEN sind die Werte im RabbitMqProperties-Record verfügbar
    @Test
    @DisplayName("valid rabbitmq properties are bound")
    void validProperties_AreBound() {
        contextRunner
                .withPropertyValues(
                        "paperless.rabbitmq.exchange=ex",
                        "paperless.rabbitmq.ocr-queue=queue",
                        "paperless.rabbitmq.ocr-routing-key=key",
                        "paperless.rabbitmq.ocr-result-queue=result-queue",
                        "paperless.rabbitmq.ocr-result-routing-key=result-key",
                        "paperless.rabbitmq.dead-letter-exchange=dlx",
                        "paperless.rabbitmq.ocr-dead-letter-queue=dlq",
                        "paperless.rabbitmq.ocr-dead-letter-routing-key=dead-key",
                        "paperless.rabbitmq.ocr-result-dead-letter-queue=result-dlq",
                        "paperless.rabbitmq.ocr-result-dead-letter-routing-key=result-dead-key")
                .run(context -> {
                    RabbitMqProperties properties = context.getBean(RabbitMqProperties.class);
                    assertNotNull(properties);
                    assertEquals("ex", properties.exchange());
                    assertEquals("queue", properties.ocrQueue());
                    assertEquals("key", properties.ocrRoutingKey());
                    assertEquals("result-queue", properties.ocrResultQueue());
                    assertEquals("result-key", properties.ocrResultRoutingKey());
                    assertEquals("dlx", properties.deadLetterExchange());
                    assertEquals("dlq", properties.ocrDeadLetterQueue());
                    assertEquals("dead-key", properties.ocrDeadLetterRoutingKey());
                    assertEquals("result-dlq", properties.ocrResultDeadLetterQueue());
                    assertEquals("result-dead-key", properties.ocrResultDeadLetterRoutingKey());
                });
    }

    // GIVEN der Queue-Name ist leer konfiguriert
    // WHEN der Kontext gestartet wird
    // THEN schlägt der Start mit einer Meldung zum Property ocrQueue fehl (Fail Fast)
    @Test
    @DisplayName("blank queue name stops the start with a clear message")
    void blankQueueName_FailsStartup() {
        contextRunner
                .withPropertyValues(
                        "paperless.rabbitmq.exchange=ex",
                        "paperless.rabbitmq.ocr-queue= ",
                        "paperless.rabbitmq.ocr-routing-key=key",
                        "paperless.rabbitmq.ocr-result-queue=result-queue",
                        "paperless.rabbitmq.ocr-result-routing-key=result-key",
                        "paperless.rabbitmq.dead-letter-exchange=dlx",
                        "paperless.rabbitmq.ocr-dead-letter-queue=dlq",
                        "paperless.rabbitmq.ocr-dead-letter-routing-key=dead-key",
                        "paperless.rabbitmq.ocr-result-dead-letter-queue=result-dlq",
                        "paperless.rabbitmq.ocr-result-dead-letter-routing-key=result-dead-key")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("paperless.rabbitmq"));
                });
    }
}
