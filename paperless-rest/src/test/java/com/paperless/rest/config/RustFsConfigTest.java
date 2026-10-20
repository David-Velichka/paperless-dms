package com.paperless.rest.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RustFsConfigTest {

    // GIVEN gültige Verbindungsparameter für RustFS
    // WHEN ein RustFsConfig Record instanziiert wird
    // THEN liefern Record-Komponenten und Getter die korrekten Werte zurück
    @Test
    void rustFsConfig_StoresAndReturnsProperties() {
        RustFsConfig config = new RustFsConfig("http://rustfs:9000", "admin", "secret", "paperless");

        assertEquals("http://rustfs:9000", config.endpoint());
        assertEquals("http://rustfs:9000", config.getEndpoint());
        assertEquals("admin", config.accessKey());
        assertEquals("admin", config.getAccessKey());
        assertEquals("secret", config.secretKey());
        assertEquals("secret", config.getSecretKey());
        assertEquals("paperless", config.bucketName());
        assertEquals("paperless", config.getBucketName());
    }
}

