package com.paperless.rest.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitMqConfigTest {

    // Same names as in application.yaml of both apps
    private final RabbitMqProperties properties = new RabbitMqProperties(
            "paperless.document.exchange", "paperless.ocr.queue", "document.ocr",
            "paperless.ocr.result.queue", "document.ocr.result", "paperless.dlx",
            "paperless.ocr.dlq", "document.ocr.dead",
            "paperless.ocr.result.dlq", "document.ocr.result.dead");

    private final RabbitMqConfig config = new RabbitMqConfig();

    // GIVEN die konfigurierten RabbitMQ-Namen
    // WHEN die OCR-Queue deklariert wird
    // THEN ist sie dauerhaft und leitet abgelehnte Jobs an die Dead-Letter-Queue weiter (gleich wie im Worker)
    @Test
    void ocrQueue_HasDeadLetterArguments() {
        Queue queue = config.ocrQueue(properties);

        assertEquals("paperless.ocr.queue", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals(Map.of("x-dead-letter-exchange", "paperless.dlx",
                "x-dead-letter-routing-key", "document.ocr.dead"), queue.getArguments());
    }

    // GIVEN die konfigurierten RabbitMQ-Namen
    // WHEN die Result-Queue deklariert wird
    // THEN ist sie dauerhaft und leitet nicht speicherbare Ergebnisse an ihre Dead-Letter-Queue weiter (gleich wie im Worker)
    @Test
    void ocrResultQueue_HasDeadLetterArguments() {
        Queue queue = config.ocrResultQueue(properties);

        assertEquals("paperless.ocr.result.queue", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals(Map.of("x-dead-letter-exchange", "paperless.dlx",
                "x-dead-letter-routing-key", "document.ocr.result.dead"), queue.getArguments());
    }
}
