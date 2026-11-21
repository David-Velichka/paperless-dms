package com.paperless.rest.messaging;

import com.paperless.rest.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the result events of the OCR worker and updates the document.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrResultListener {

    private final DocumentService documentService;

    @RabbitListener(queues = "${paperless.rabbitmq.ocr-result-queue}")
    public void receiveOcrResult(OcrResultMessage message) {
        if (message == null || message.documentId() == null || message.status() == null) {
            // The message itself is not logged, it can contain the whole OCR text
            log.warn("Rejected invalid OCR result without document id or status");
            throw new AmqpRejectAndDontRequeueException("OCR result without document id or status");
        }

        log.info("Received OCR result documentId={} status={}", message.documentId(), message.status());

        switch (message.status()) {
            case STARTED -> documentService.markOcrStarted(message.documentId());
            case COMPLETED -> documentService.completeOcr(message.documentId(), message.ocrText());
        }
    }
}
