package com.paperless.rest.messaging;

import com.paperless.rest.config.RabbitMqConfig;
import com.paperless.rest.exception.MessageQueueException;
import com.paperless.rest.service.model.DocumentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Publishes OCR jobs for uploaded documents to RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOcrMessage(DocumentModel document) {
        OcrMessage message = new OcrMessage(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                OffsetDateTime.now());
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.DOCUMENT_EXCHANGE, RabbitMqConfig.OCR_ROUTING_KEY, message);
            log.info("OCR message sent for document {} to queue {}", document.getId(), RabbitMqConfig.OCR_QUEUE);
        } catch (AmqpException ex) {
            log.error("Failed to send OCR message for document {}", document.getId(), ex);
            throw new MessageQueueException("Document " + document.getId() + " could not be queued for OCR", ex);
        }
    }
}
