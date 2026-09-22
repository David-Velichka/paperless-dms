package com.paperless.rest.service;

import com.paperless.rest.service.model.DocumentModel;

import java.util.List;

/**
 * Service interface for business logic operations on documents.
 * Ensures loose coupling between Presentation and Business layers.
 */
public interface DocumentService {
    DocumentModel save(DocumentModel documentModel);
    DocumentModel findById(Long id);
    List<DocumentModel> findAll();
    List<DocumentModel> search(String query);
    void deleteById(Long id);
    DocumentModel uploadDocument(DocumentModel documentModel, byte[] content);
}
