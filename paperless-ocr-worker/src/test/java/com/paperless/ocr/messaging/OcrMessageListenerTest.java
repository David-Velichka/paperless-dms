package com.paperless.ocr.messaging;

import com.paperless.ocr.exception.DocumentNotFoundInStorageException;
import com.paperless.ocr.exception.StorageException;
import com.paperless.ocr.service.OcrJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OcrMessageListenerTest {

    @Mock
    private OcrJobService ocrJobService;

    @InjectMocks
    private OcrMessageListener listener;

    // GIVEN eine gültige OCR-Nachricht mit Dokument-ID und Storage-Key
    // WHEN der Listener die Nachricht empfängt
    // THEN wird der OCR-Job an den OcrJobService übergeben
    @Test
    void receiveOcrMessage_WithValidMessage_CallsService() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now(), "abc.pdf");

        assertDoesNotThrow(() -> listener.receiveOcrMessage(message));

        verify(ocrJobService).process(message);
    }

    // GIVEN eine OCR-Nachricht ohne Dokument-ID
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WithoutDocumentId_RejectsWithoutRequeue() {
        OcrMessage message = new OcrMessage(null, "invoice.pdf", "application/pdf", OffsetDateTime.now(), "abc.pdf");

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(message));

        verifyNoInteractions(ocrJobService);
    }

    // GIVEN eine OCR-Nachricht ohne Storage-Key
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WithoutStorageKey_RejectsWithoutRequeue() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now(), " ");

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(message));

        verifyNoInteractions(ocrJobService);
    }

    // GIVEN eine leere Nachricht (null)
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WithNullMessage_RejectsWithoutRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(null));

        verify(ocrJobService, never()).process(null);
    }

    // GIVEN das PDF existiert nicht mehr im Storage (Dokument wurde gelöscht)
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrMessage_WhenFileMissingInStorage_RejectsWithoutRequeue() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now(), "abc.pdf");
        doThrow(new DocumentNotFoundInStorageException("missing", null)).when(ocrJobService).process(message);

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrMessage(message));
    }

    // GIVEN der Storage ist nicht erreichbar
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Exception weitergegeben, damit der Retry der Queue greift
    @Test
    void receiveOcrMessage_WhenStorageFails_PropagatesException() {
        OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now(), "abc.pdf");
        doThrow(new StorageException("down", null)).when(ocrJobService).process(message);

        assertThrows(StorageException.class, () -> listener.receiveOcrMessage(message));
    }
}
