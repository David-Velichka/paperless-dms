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
        if (documentModel == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        if (documentModel.getTitle() == null || documentModel.getTitle().isBlank()) {
            throw new BusinessLayerException("Document title must not be blank");
        }
        log.info("Saving document model: {}", documentModel.getTitle());
        DocumentEntity entity = documentMapper.toEntity(documentModel);
        DocumentEntity saved = documentRepository.save(entity);
        return documentMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
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
        if (query == null || query.isBlank()) {
            return findAll();
        }
        List<DocumentEntity> entities = documentRepository.findByTitleContainingIgnoreCase(query);
        return documentMapper.toModelList(entities);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        log.info("Deleting document by id: {}", id);
        if (!documentRepository.existsById(id)) {
            throw new BusinessLayerException("Cannot delete: Document with id " + id + " does not exist");
        }
        documentRepository.deleteById(id);
    }

    @Override
    public DocumentModel uploadDocument(DocumentModel documentModel, byte[] content) {
        if (documentModel == null) {
            throw new BusinessLayerException("Document model must not be null");
        }
        if (documentModel.getTitle() == null || documentModel.getTitle().isBlank()) {
            throw new BusinessLayerException("Document title must not be blank");
        }
        if (content == null || content.length == 0) {
            throw new BusinessLayerException("Document file content must not be empty");
        }
        log.info("Processing upload for document: {}", documentModel.getTitle());
        DocumentEntity entity = documentMapper.toEntity(documentModel);
        DocumentEntity saved = documentRepository.save(entity);
        DocumentModel savedModel = documentMapper.toModel(saved);

        log.info("Document successfully initiated with id: {}", saved.getId());
        return savedModel;
    }
}
