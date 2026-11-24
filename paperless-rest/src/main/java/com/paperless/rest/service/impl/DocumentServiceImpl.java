package com.paperless.rest.service.impl;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.DocumentNotFoundException;
import com.paperless.rest.service.DocumentSearchIndex;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.DocumentStorage;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.OcrJobPublisher;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.validation.DocumentModelValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentPersistenceService documentPersistenceService;
    private final DocumentStatusHistoryService documentStatusHistoryService;
    private final DocumentModelValidator documentModelValidator;
    private final OcrJobPublisher ocrJobPublisher;
    private final DocumentStorage documentStorage;
    private final DocumentSearchIndex documentSearchIndex;

    @Override
    public DocumentModel save(DocumentModel documentModel) {
        if (documentModel == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        documentModelValidator.validateOrThrow(documentModel);

        log.info("Saving document model title='{}'", documentModel.getTitle());
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
        log.info("Updating document metadata documentId={}", id);
        DocumentModel updated = existing.toBuilder().title(changes.getTitle()).build();
        return save(updated);
    }

    @Override
    public DocumentModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        log.debug("Finding document by id documentId={}", id);
        return documentPersistenceService.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document with id " + id + " not found"));
    }

    @Override
    public List<DocumentModel> findAll() {
        log.debug("Finding all documents");
        return documentPersistenceService.findAll();
    }

    // Elasticsearch finds the OCR text and the file name, the database the title.
    // Documents are always loaded from the database, so a deleted document never shows up.
    // If Elasticsearch fails, the circuit breaker falls back to the title search.
    @Override
    @CircuitBreaker(name = "search", fallbackMethod = "searchByTitleOnly")
    public List<DocumentModel> search(String query) {
        log.info("Searching documents query='{}'", query);
        if (query == null || query.isBlank()) {
            return findAll();
        }
        List<Long> indexIds = documentSearchIndex.search(query);

        // Keep the relevance order of Elasticsearch, ids without a database row are dropped
        Map<Long, DocumentModel> byId = new HashMap<>();
        for (DocumentModel document : documentPersistenceService.findAllByIds(indexIds)) {
            byId.put(document.getId(), document);
        }
        Map<Long, DocumentModel> results = new LinkedHashMap<>();
        for (Long id : indexIds) {
            if (byId.containsKey(id)) {
                results.put(id, byId.get(id));
            }
        }
        // Title matches that Elasticsearch did not find are appended
        for (DocumentModel document : documentPersistenceService.searchByTitle(query)) {
            results.putIfAbsent(document.getId(), document);
        }
        return new ArrayList<>(results.values());
    }

    // Fallback of the search circuit breaker: degraded search on the title only
    private List<DocumentModel> searchByTitleOnly(String query, Throwable cause) {
        log.warn("Search index unavailable, falling back to title search query='{}' reason={}",
                query, cause.toString());
        return documentPersistenceService.searchByTitle(query);
    }

    // One transaction: the status history is only deleted together with the document.
    // The file is deleted after the rows: if the storage fails, the transaction is rolled back and nothing is lost.
    // The index entry is deleted last and only best effort: a stale entry never shows up, the search loads from the database.
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            log.debug("Document id is null, ignoring delete request");
            return;
        }
        log.info("Deleting document by id documentId={}", id);
        Optional<DocumentModel> document = documentPersistenceService.findById(id);
        if (document.isEmpty()) {
            log.debug("Document does not exist, deletion is idempotent documentId={}", id);
            return;
        }
        documentStatusHistoryService.deleteByDocumentId(id);
        documentPersistenceService.deleteById(id);

        String storagePath = document.get().getStoragePath();
        if (storagePath != null && !storagePath.isBlank()) {
            documentStorage.delete(storagePath);
        }

        try {
            documentSearchIndex.delete(id);
        } catch (RuntimeException ex) {
            log.warn("Search index entry could not be deleted documentId={}", id, ex);
        }
    }

    // S3 has no transactions: the file is stored first, then metadata, history and OCR message are saved in one
    // database transaction. A callback deletes the file again whenever Spring reports a rollback, also for a commit
    // that fails and is rolled back. With an unknown commit outcome the file is kept on purpose: the row may exist,
    // and a row without its file is worse than an unused file.
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

        log.info("Uploading document title='{}' sizeBytes={}", documentModel.getTitle(), content.length);

        String storageKey = UUID.randomUUID() + ".pdf";
        documentStorage.store(storageKey, content, documentModel.getContentType());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteStoredFileQuietly(storageKey);
                }
            }
        });

        DocumentModel toSave = documentModel.toBuilder().storagePath(storageKey).build();
        if (toSave.getCurrentStatus() == null) {
            toSave = toSave.toBuilder().currentStatus(DocumentStatus.RECEIVED).build();
        }
        DocumentModel saved = documentPersistenceService.save(toSave);

        documentStatusHistoryService.createInitialHistory(saved.getId(), "Document file uploaded");

        ocrJobPublisher.sendOcrMessage(saved);

        return saved;
    }

    // Only a RECEIVED document moves on, a repeated event (retry of the OCR job) is ignored.
    // Statuses are checked before changing them: an exception from changeStatus would roll back the transaction.
    @Override
    @Transactional
    public void markOcrStarted(Long id) {
        Optional<DocumentModel> document = documentPersistenceService.findById(id);
        if (document.isEmpty()) {
            log.warn("Ignored OCR start for a missing document documentId={}", id);
            return;
        }
        DocumentStatus status = document.get().getCurrentStatus();
        if (status != DocumentStatus.RECEIVED) {
            log.info("Ignored OCR start documentId={} status={}", id, status);
            return;
        }
        documentStatusHistoryService.changeStatus(id, DocumentStatus.PROCESSING, "OCR started");
    }

    // The text is stored first: changeStatus loads and saves the document itself,
    // saving the model loaded here after it would fail with an optimistic lock conflict.
    @Override
    @Transactional
    public void completeOcr(Long id, String ocrText) {
        Optional<DocumentModel> document = documentPersistenceService.findById(id);
        if (document.isEmpty()) {
            log.warn("Ignored OCR result for a missing document documentId={}", id);
            return;
        }
        documentPersistenceService.save(document.get().toBuilder().ocrText(ocrText).build());
        log.info("OCR text stored documentId={} textLength={}", id, ocrText == null ? 0 : ocrText.length());

        DocumentStatus status = document.get().getCurrentStatus();
        if (status == DocumentStatus.RECEIVED) {
            // The STARTED event was missed or arrives later, so both steps are done here
            documentStatusHistoryService.changeStatus(id, DocumentStatus.PROCESSING, "OCR started");
            status = DocumentStatus.PROCESSING;
        }
        if (status == DocumentStatus.PROCESSING) {
            documentStatusHistoryService.changeStatus(id, DocumentStatus.REVIEW_PENDING, "OCR completed");
        } else {
            log.info("Status kept after OCR documentId={} status={}", id, status);
        }
    }

    // Compensation for a rolled back upload, best effort: a failing delete only leaves an orphan file
    private void deleteStoredFileQuietly(String storageKey) {
        try {
            documentStorage.delete(storageKey);
            log.info("Stored file deleted after rolled back upload key={}", storageKey);
        } catch (RuntimeException ex) {
            log.warn("Orphan file could not be deleted after failed upload key={}", storageKey, ex);
        }
    }
}
