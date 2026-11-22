package com.paperless.ocr.messaging;

import com.paperless.ocr.config.RabbitMqProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitOcrResultPublisherTest {

    private static final RabbitMqProperties PROPERTIES =
            new RabbitMqProperties("test.exchange", "test.queue", "test.result.queue", "test.result",
                    "test.dlx", "test.dlq", "test.dead");

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitOcrResultPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitOcrResultPublisher(rabbitTemplate, PROPERTIES);
    }

    // GIVEN ein OCR-Ergebnis für ein Dokument
    // WHEN publish aufgerufen wird
    // THEN wird die Nachricht mit dem Result-Routing-Key an den Exchange gesendet
    @Test
    void publish_SendsMessageWithResultRoutingKey() {
        OcrResultMessage message = new OcrResultMessage(1L, OcrResultStatus.COMPLETED, "Hello");

        publisher.publish(message);

        verify(rabbitTemplate).convertAndSend("test.exchange", "test.result", message);
    }

    // GIVEN RabbitMQ ist nicht erreichbar
    // WHEN publish aufgerufen wird
    // THEN wird die Exception weitergegeben, damit der OCR-Job wiederholt wird
    @Test
    void publish_WhenBrokerUnavailable_PropagatesException() {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertThrows(AmqpConnectException.class,
                () -> publisher.publish(new OcrResultMessage(1L, OcrResultStatus.STARTED, null)));
    }
}
