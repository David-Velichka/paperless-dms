package com.paperless.rest.service;

import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;

import java.util.List;

/**
 * Service interface for business logic operations on document status lifecycle and audit log.
 */
public interface DocumentStatusHistoryService {
    DocumentStatusHistoryModel changeStatus(Long documentId, DocumentStatus targetStatus, String comment);
    List<DocumentStatusHistoryModel> getHistoryByDocumentId(Long documentId);
    DocumentStatusHistoryModel createInitialHistory(Long documentId, String comment);
    DocumentStatusHistoryModel findById(Long id);
    void deleteByDocumentId(Long documentId);
}
