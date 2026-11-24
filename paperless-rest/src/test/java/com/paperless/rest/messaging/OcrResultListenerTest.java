package com.paperless.rest.messaging;

import com.paperless.rest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OcrResultListenerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private OcrResultListener listener;

    // GIVEN ein STARTED-Event für ein Dokument
    // WHEN der Listener die Nachricht empfängt
    // THEN wird markOcrStarted im Service aufgerufen
    @Test
    void receiveOcrResult_WithStarted_CallsMarkOcrStarted() {
        listener.receiveOcrResult(new OcrResultMessage(1L, OcrResultStatus.STARTED, null));

        verify(documentService).markOcrStarted(1L);
        verifyNoMoreInteractions(documentService);
    }

    // GIVEN ein COMPLETED-Event mit erkanntem Text
    // WHEN der Listener die Nachricht empfängt
    // THEN wird completeOcr mit dem Text im Service aufgerufen
    @Test
    void receiveOcrResult_WithCompleted_CallsCompleteOcr() {
        listener.receiveOcrResult(new OcrResultMessage(1L, OcrResultStatus.COMPLETED, "Hello World"));

        verify(documentService).completeOcr(1L, "Hello World");
        verifyNoMoreInteractions(documentService);
    }

    // GIVEN ein Event ohne Dokument-ID
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrResult_WithoutDocumentId_RejectsWithoutRequeue() {
        OcrResultMessage message = new OcrResultMessage(null, OcrResultStatus.COMPLETED, "Hello");

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrResult(message));

        verifyNoInteractions(documentService);
    }

    // GIVEN ein Event ohne Status
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrResult_WithoutStatus_RejectsWithoutRequeue() {
        OcrResultMessage message = new OcrResultMessage(1L, null, null);

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrResult(message));

        verifyNoInteractions(documentService);
    }

    // GIVEN eine leere Nachricht (null)
    // WHEN der Listener die Nachricht empfängt
    // THEN wird die Nachricht abgelehnt und nicht erneut in die Queue gestellt
    @Test
    void receiveOcrResult_WithNullMessage_RejectsWithoutRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveOcrResult(null));

        verifyNoInteractions(documentService);
    }
}
