package com.paperless.rest.service.impl;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.model.DocumentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public DocumentModel save(DocumentModel documentModel) {
        log.info("Saving document model: {}", documentModel.getTitle());
        DocumentEntity entity = documentMapper.toEntity(documentModel);
        DocumentEntity saved = documentRepository.save(entity);
        return documentMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentModel findById(Long id) {
        log.debug("Finding document by id: {}", id);
        return documentRepository.findById(id)
                .map(documentMapper::toModel)
                .orElseThrow(() -> new BusinessLayerException("Document with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentModel> findAll() {
        log.debug("Finding all documents");
        return documentMapper.toModelList(documentRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentModel> search(String query) {
        log.info("Searching documents with query: {}", query);
        List<DocumentEntity> entities = documentRepository.findByTitleContainingIgnoreCase(query);
        return documentMapper.toModelList(entities);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting document by id: {}", id);
        if (!documentRepository.existsById(id)) {
            throw new BusinessLayerException("Cannot delete: Document with id " + id + " does not exist");
        }
        documentRepository.deleteById(id);
    }

    @Override
    public DocumentModel uploadDocument(DocumentModel documentModel, byte[] content) {
        log.info("Processing upload for document: {}", documentModel.getTitle());
        // Business logic workflow:
        // 1. Persist initial document metadata via repository
        DocumentEntity entity = documentMapper.toEntity(documentModel);
        DocumentEntity saved = documentRepository.save(entity);
        DocumentModel savedModel = documentMapper.toModel(saved);

        // 2. Storage & Asynchronous message queue dispatch will be invoked here (Sprint 3/4)
        log.info("Document successfully initiated with id: {}", saved.getId());
        return savedModel;
    }
}
