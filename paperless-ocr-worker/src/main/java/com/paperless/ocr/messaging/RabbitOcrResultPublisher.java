package com.paperless.ocr.messaging;

import com.paperless.ocr.config.RabbitMqProperties;
import com.paperless.ocr.service.OcrResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes OCR result events to the result queue of paperless-rest.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitOcrResultPublisher implements OcrResultPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;

    @Override
    public void publish(OcrResultMessage message) {
        // A failure propagates, so the OCR job is retried by the listener
        rabbitTemplate.convertAndSend(rabbitMqProperties.exchange(), rabbitMqProperties.ocrResultRoutingKey(), message);
        log.info("OCR result sent documentId={} status={}", message.documentId(), message.status());
    }
}
