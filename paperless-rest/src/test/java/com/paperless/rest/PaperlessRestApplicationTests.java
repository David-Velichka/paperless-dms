package com.paperless.rest;

import com.paperless.rest.service.DocumentStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PaperlessRestApplicationTests {

    // No RustFS in the test run: the real storage would connect to it at startup
    @MockitoBean
    private DocumentStorage documentStorage;

    // GIVEN ein vollständig konfigurierter Spring Boot Anwendungskontext
    // WHEN der Anwendungskontext geladen wird
    // THEN lädt die Anwendung erfolgreich ohne Fehler
    @Test
    void contextLoads() {
    }

}
