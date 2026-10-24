package com.paperless.ocr.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OcrMessageListenerTest {

    private final OcrMessageListener listener = new OcrMessageListener();

    // GIVEN eine gültige OCR-Nachricht mit Dokument-ID
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht ohne Fehler verarbeitet (geloggt)
    @Test
    void receiveOcrMessage_WithValidMessage_ProcessesWithoutException() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now());

        assertDoesNotThrow(() -> listener.receiveOcrMessage(message));
    }

    // GIVEN eine OCR-Nachricht ohne Dokument-ID
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WithoutDocumentId_RejectsWithoutRequeue() {
        OcrMessage message = new OcrMessage(null, "invoice.pdf", "application/pdf", OffsetDateTime.now());

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(message));
    }

    // GIVEN eine leere Nachricht (null)
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WithNullMessage_RejectsWithoutRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(null));
    }
}
