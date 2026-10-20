package com.paperless.rest.service.impl;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.dal.persistence.DocumentStatusHistoryPersistenceService;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.DocumentNotFoundException;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import com.paperless.rest.service.validation.DocumentStatusHistoryModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusHistoryServiceImpl implements DocumentStatusHistoryService {

    private final DocumentPersistenceService documentPersistenceService;
    private final DocumentStatusHistoryPersistenceService documentStatusHistoryPersistenceService;
    private final DocumentStatusHistoryModelValidator historyModelValidator;

    @Override
    public DocumentStatusHistoryModel changeStatus(Long documentId, DocumentStatus targetStatus, String comment) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        if (targetStatus == null) {
            throw new BusinessLayerException("Target status must not be null");
        }

        DocumentModel documentModel = documentPersistenceService.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document with id " + documentId + " not found"));

        DocumentStatus previousStatus = documentModel.getCurrentStatus() != null ? documentModel.getCurrentStatus() : DocumentStatus.RECEIVED;

        log.info("Transitioning document {} status from {} to {}", documentId, previousStatus, targetStatus);

        documentModel.transitionTo(targetStatus);

        DocumentStatusHistoryModel historyModel = DocumentStatusHistoryModel.builder()
                .documentId(documentId)
                .status(targetStatus)
                .previousStatus(previousStatus)
                .timestamp(OffsetDateTime.now())
                .comment(comment != null ? comment.trim() : null)
                .build();

        historyModelValidator.validateOrThrow(historyModel);

        documentPersistenceService.save(documentModel);

        return documentStatusHistoryPersistenceService.save(historyModel);
    }

    @Override
    public List<DocumentStatusHistoryModel> getHistoryByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        if (!documentPersistenceService.existsById(documentId)) {
            throw new DocumentNotFoundException("Document with id " + documentId + " not found");
        }
        log.debug("Fetching status history for document id: {}", documentId);
        return documentStatusHistoryPersistenceService.findByDocumentIdOrderByTimestampDesc(documentId);
    }

    @Override
    public DocumentStatusHistoryModel createInitialHistory(Long documentId, String comment) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        String note = (comment != null && !comment.isBlank()) ? comment.trim() : "Initial upload";

        DocumentStatusHistoryModel historyModel = DocumentStatusHistoryModel.builder()
                .documentId(documentId)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .timestamp(OffsetDateTime.now())
                .comment(note)
                .build();

        historyModelValidator.validateOrThrow(historyModel);

        return documentStatusHistoryPersistenceService.save(historyModel);
    }

    @Override
    public DocumentStatusHistoryModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("History id must not be null");
        }
        return documentStatusHistoryPersistenceService.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("DocumentStatusHistory with id " + id + " not found"));
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        documentStatusHistoryPersistenceService.deleteByDocumentId(documentId);
    }
}
