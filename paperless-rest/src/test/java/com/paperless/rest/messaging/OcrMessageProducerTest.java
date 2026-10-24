package com.paperless.rest.messaging;

import com.paperless.rest.config.RabbitMqConfig;
import com.paperless.rest.exception.MessageQueueException;
import com.paperless.rest.service.model.DocumentModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OcrMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OcrMessageProducer ocrMessageProducer;

    private final DocumentModel document = DocumentModel.builder()
            .id(42L)
            .title("Invoice")
            .originalFilename("invoice.pdf")
            .contentType("application/pdf")
            .build();

    // GIVEN ein gespeichertes Dokument
    // WHEN sendOcrMessage aufgerufen wird
    // THEN wird eine OcrMessage mit Dokument-ID und Dateiname an den OCR-Exchange gesendet
    @Test
    void sendOcrMessage_SendsMessageToOcrExchange() {
        ocrMessageProducer.sendOcrMessage(document);

        ArgumentCaptor<OcrMessage> captor = ArgumentCaptor.forClass(OcrMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.DOCUMENT_EXCHANGE), eq(RabbitMqConfig.OCR_ROUTING_KEY), captor.capture());
        OcrMessage message = captor.getValue();
        assertEquals(42L, message.documentId());
        assertEquals("invoice.pdf", message.originalFilename());
        assertEquals("application/pdf", message.contentType());
        assertNotNull(message.uploadedAt());
    }

    // GIVEN RabbitMQ ist nicht erreichbar
    // WHEN sendOcrMessage aufgerufen wird
    // THEN wird die AmqpException in eine MessageQueueException umgewandelt
    @Test
    void sendOcrMessage_WhenBrokerUnavailable_ThrowsMessageQueueException() {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        MessageQueueException ex = assertThrows(MessageQueueException.class,
                () -> ocrMessageProducer.sendOcrMessage(document));
        assertInstanceOf(AmqpConnectException.class, ex.getCause());
    }
}
