package com.paperless.rest.service.impl;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.DocumentNotFoundException;
import com.paperless.rest.messaging.OcrMessageProducer;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.validation.DocumentModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentPersistenceService documentPersistenceService;
    private final DocumentStatusHistoryService documentStatusHistoryService;
    private final DocumentModelValidator documentModelValidator;
    private final OcrMessageProducer ocrMessageProducer;

    @Override
    public DocumentModel save(DocumentModel documentModel) {
        if (documentModel == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        documentModelValidator.validateOrThrow(documentModel);

        log.info("Saving document model: {}", documentModel.getTitle());
        boolean isNew = (documentModel.getId() == null);
        if (isNew && documentModel.getCurrentStatus() == null) {
            documentModel = documentModel.toBuilder().currentStatus(DocumentStatus.RECEIVED).build();
        }
        DocumentModel saved = documentPersistenceService.save(documentModel);
        if (isNew) {
            documentStatusHistoryService.createInitialHistory(saved.getId(), "Document metadata created");
        }
        return saved;
    }

    @Override
    public DocumentModel update(Long id, DocumentModel changes) {
        if (changes == null) {
            throw new BusinessLayerException("Document changes must not be null");
        }
        DocumentModel existing = findById(id);
        log.info("Updating metadata of document {}", id);
        DocumentModel updated = existing.toBuilder().title(changes.getTitle()).build();
        return save(updated);
    }

    @Override
    public DocumentModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        log.debug("Finding document by id: {}", id);
        return documentPersistenceService.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document with id " + id + " not found"));
    }

    @Override
    public List<DocumentModel> findAll() {
        log.debug("Finding all documents");
        return documentPersistenceService.findAll();
    }

    @Override
    public List<DocumentModel> search(String query) {
        log.info("Searching documents with query: {}", query);
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return documentPersistenceService.searchByTitle(query);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            log.debug("Document id is null, ignoring delete request");
            return;
        }
        log.info("Deleting document by id: {}", id);
        if (!documentPersistenceService.existsById(id)) {
            log.debug("Document with id {} does not exist, deletion is idempotent", id);
            return;
        }
        documentStatusHistoryService.deleteByDocumentId(id);
        documentPersistenceService.deleteById(id);
    }

    // One transaction for metadata, history and OCR message: if the queue is not reachable, nothing is saved
    @Override
    @Transactional
    public DocumentModel uploadDocument(DocumentModel documentModel, byte[] content) {
        if (documentModel == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        if (content == null || content.length == 0) {
            throw new BusinessLayerException("Document file content must not be empty");
        }
        documentModelValidator.validateOrThrow(documentModel);

        log.info("Uploading document: {} (size: {} bytes)", documentModel.getTitle(), content.length);

        if (documentModel.getCurrentStatus() == null) {
            documentModel = documentModel.toBuilder().currentStatus(DocumentStatus.RECEIVED).build();
        }
        DocumentModel saved = documentPersistenceService.save(documentModel);

        documentStatusHistoryService.createInitialHistory(saved.getId(), "Document file uploaded");

        ocrMessageProducer.sendOcrMessage(saved);

        return saved;
    }
}
