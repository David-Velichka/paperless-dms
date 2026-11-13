package com.paperless.rest.messaging;

import com.paperless.rest.config.RabbitMqProperties;
import com.paperless.rest.exception.MessageQueueException;
import com.paperless.rest.service.OcrJobPublisher;
import com.paperless.rest.service.model.DocumentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Publishes OCR jobs for uploaded documents to RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrMessageProducer implements OcrJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;

    @Override
    public void sendOcrMessage(DocumentModel document) {
        OcrMessage message = new OcrMessage(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                OffsetDateTime.now(ZoneOffset.UTC));
        try {
            rabbitTemplate.convertAndSend(rabbitMqProperties.exchange(), rabbitMqProperties.ocrRoutingKey(), message);
            log.info("OCR message sent documentId={} queue={}", document.getId(), rabbitMqProperties.ocrQueue());
        } catch (AmqpException ex) {
            log.error("Failed to send OCR message documentId={}", document.getId(), ex);
            throw new MessageQueueException("Document " + document.getId() + " could not be queued for OCR", ex);
        }
    }
}
