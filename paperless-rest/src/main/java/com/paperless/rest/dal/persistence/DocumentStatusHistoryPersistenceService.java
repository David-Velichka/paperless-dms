package com.paperless.rest.dal.persistence;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.mapper.DocumentStatusHistoryEntityMapper;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Dedicated persistence service (Anti-Corruption Layer) for DocumentStatusHistory entities.
 * Encapsulates JPA entity mapping and repository calls with transaction demarcation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusHistoryPersistenceService {

    private final DocumentStatusHistoryRepository documentStatusHistoryRepository;
    private final DocumentStatusHistoryEntityMapper documentStatusHistoryEntityMapper;

    @Transactional
    public DocumentStatusHistoryModel save(DocumentStatusHistoryModel model) {
        DocumentStatusHistoryEntity entity = documentStatusHistoryEntityMapper.toEntity(model);
        DocumentStatusHistoryEntity saved = documentStatusHistoryRepository.save(entity);
        return documentStatusHistoryEntityMapper.toModel(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusHistoryModel> findByDocumentIdOrderByTimestampDesc(Long documentId) {
        List<DocumentStatusHistoryEntity> entities = documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(documentId);
        return documentStatusHistoryEntityMapper.toModelList(entities);
    }

    @Transactional(readOnly = true)
    public Optional<DocumentStatusHistoryModel> findById(Long id) {
        return documentStatusHistoryRepository.findById(id).map(documentStatusHistoryEntityMapper::toModel);
    }

    @Transactional
    public void deleteByDocumentId(Long documentId) {
        if (documentId != null) {
            documentStatusHistoryRepository.deleteByDocumentId(documentId);
            log.info("Status history for document {} deleted from database", documentId);
        }
    }
}
