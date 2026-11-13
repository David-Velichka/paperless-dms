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

    // GIVEN alle drei RabbitMQ-Namen sind konfiguriert
    // WHEN der Kontext gestartet wird
    // THEN sind die Werte im RabbitMqProperties-Record verfügbar
    @Test
    @DisplayName("valid rabbitmq properties are bound")
    void validProperties_AreBound() {
        contextRunner
                .withPropertyValues(
                        "paperless.rabbitmq.exchange=ex",
                        "paperless.rabbitmq.ocr-queue=queue",
                        "paperless.rabbitmq.ocr-routing-key=key")
                .run(context -> {
                    RabbitMqProperties properties = context.getBean(RabbitMqProperties.class);
                    assertNotNull(properties);
                    assertEquals("ex", properties.exchange());
                    assertEquals("queue", properties.ocrQueue());
                    assertEquals("key", properties.ocrRoutingKey());
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
                        "paperless.rabbitmq.ocr-routing-key=key")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("paperless.rabbitmq"));
                });
    }
}
