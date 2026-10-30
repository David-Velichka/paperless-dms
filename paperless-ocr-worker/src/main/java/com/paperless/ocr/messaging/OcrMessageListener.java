package com.paperless.ocr.messaging;

import com.paperless.ocr.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes OCR jobs from the queue. OCR itself is not implemented yet, the job is only logged.
 */
@Component
@Slf4j
public class OcrMessageListener {

    @RabbitListener(queues = RabbitMqConfig.OCR_QUEUE)
    public void receiveOcrMessage(OcrMessage message) {
        if (message == null || message.documentId() == null) {
            log.error("Rejected invalid OCR message without document id: {}", message);
            // Do not requeue, otherwise the broken message would be redelivered endlessly
            throw new AmqpRejectAndDontRequeueException("OCR message without document id");
        }

        log.info("Received OCR job for document {} (file: {}, type: {}, uploaded at: {})",
                message.documentId(), message.originalFilename(), message.contentType(), message.uploadedAt());
    }
}
