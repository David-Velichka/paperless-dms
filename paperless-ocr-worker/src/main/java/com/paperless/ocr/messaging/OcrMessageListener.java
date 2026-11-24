package com.paperless.ocr.messaging;

import com.paperless.ocr.exception.DocumentNotFoundInStorageException;
import com.paperless.ocr.service.OcrJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes OCR jobs from the queue and hands them to the OCR job service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrMessageListener {

    private final OcrJobService ocrJobService;

    @RabbitListener(queues = "${paperless.rabbitmq.ocr-queue}")
    public void receiveOcrMessage(OcrMessage message) {
        if (message == null || message.documentId() == null
                || message.storageKey() == null || message.storageKey().isBlank()) {
            log.error("Rejected invalid OCR message without document id or storage key message={}", message);
            // Do not requeue, otherwise the broken message would be redelivered endlessly
            throw new AmqpRejectAndDontRequeueException("OCR message without document id or storage key");
        }

        log.info("Received OCR job documentId={} filename='{}' contentType={} uploadedAt={} storageKey={}",
                message.documentId(), message.originalFilename(), message.contentType(), message.uploadedAt(),
                message.storageKey());

        try {
            ocrJobService.process(message);
        } catch (DocumentNotFoundInStorageException ex) {
            // The document was probably deleted meanwhile, a retry cannot succeed
            log.warn("Dropped OCR job because the file is missing documentId={} storageKey={}",
                    message.documentId(), message.storageKey());
            throw new AmqpRejectAndDontRequeueException("Document file missing in the storage", ex);
        }
    }
}
