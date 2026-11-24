package com.paperless.rest.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RustFsPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(RustFsProperties.class)
    static class PropertiesConfig {
    }

    // GIVEN alle RustFS-Werte inklusive Timeouts sind konfiguriert
    // WHEN der Kontext gestartet wird
    // THEN sind die Werte im RustFsProperties-Record verfügbar
    @Test
    @DisplayName("valid rustfs properties are bound")
    void validProperties_AreBound() {
        contextRunner
                .withPropertyValues(
                        "paperless.rustfs.endpoint=http://rustfs:9000",
                        "paperless.rustfs.access-key=access",
                        "paperless.rustfs.secret-key=secret",
                        "paperless.rustfs.bucket=documents",
                        "paperless.rustfs.api-call-timeout=120s",
                        "paperless.rustfs.api-call-attempt-timeout=30s")
                .run(context -> {
                    RustFsProperties properties = context.getBean(RustFsProperties.class);
                    assertNotNull(properties);
                    assertEquals("http://rustfs:9000", properties.endpoint());
                    assertEquals("access", properties.accessKey());
                    assertEquals("secret", properties.secretKey());
                    assertEquals("documents", properties.bucket());
                    assertEquals(Duration.ofSeconds(120), properties.apiCallTimeout());
                    assertEquals(Duration.ofSeconds(30), properties.apiCallAttemptTimeout());
                });
    }

    // GIVEN der Secret Key ist leer konfiguriert
    // WHEN der Kontext gestartet wird
    // THEN schlägt der Start mit einer Meldung zu paperless.rustfs fehl (Fail Fast)
    @Test
    @DisplayName("blank secret key stops the start with a clear message")
    void blankSecretKey_FailsStartup() {
        contextRunner
                .withPropertyValues(
                        "paperless.rustfs.endpoint=http://rustfs:9000",
                        "paperless.rustfs.access-key=access",
                        "paperless.rustfs.secret-key= ",
                        "paperless.rustfs.bucket=documents",
                        "paperless.rustfs.api-call-timeout=120s",
                        "paperless.rustfs.api-call-attempt-timeout=30s")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("paperless.rustfs"));
                });
    }

    // GIVEN der Timeout für einen einzelnen Versuch fehlt
    // WHEN der Kontext gestartet wird
    // THEN schlägt der Start mit einer Meldung zu paperless.rustfs fehl (Fail Fast)
    @Test
    @DisplayName("missing attempt timeout stops the start with a clear message")
    void missingAttemptTimeout_FailsStartup() {
        contextRunner
                .withPropertyValues(
                        "paperless.rustfs.endpoint=http://rustfs:9000",
                        "paperless.rustfs.access-key=access",
                        "paperless.rustfs.secret-key=secret",
                        "paperless.rustfs.bucket=documents",
                        "paperless.rustfs.api-call-timeout=120s")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("paperless.rustfs"));
                });
    }
}
