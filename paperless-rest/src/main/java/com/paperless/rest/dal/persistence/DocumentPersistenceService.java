package com.paperless.rest.dal.persistence;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.mapper.DocumentEntityMapper;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.service.model.DocumentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Dedicated persistence service (Anti-Corruption Layer) for Document entities.
 * Encapsulates JPA entity mapping and repository calls with transaction demarcation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentPersistenceService {

    private final DocumentRepository documentRepository;
    private final DocumentEntityMapper documentEntityMapper;

    @Transactional
    public DocumentModel save(DocumentModel model) {
        DocumentEntity entity = documentEntityMapper.toEntity(model);
        // Flush now, so version and modifiedAt in the returned model are already updated
        DocumentEntity saved = documentRepository.saveAndFlush(entity);
        return documentEntityMapper.toModel(saved);
    }

    @Transactional(readOnly = true)
    public Optional<DocumentModel> findById(Long id) {
        return documentRepository.findById(id).map(documentEntityMapper::toModel);
    }

    @Transactional(readOnly = true)
    public List<DocumentModel> findAll() {
        return documentEntityMapper.toModelList(documentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<DocumentModel> searchByTitle(String query) {
        return documentEntityMapper.toModelList(documentRepository.findByTitleContainingIgnoreCase(query));
    }

    @Transactional
    public void deleteById(Long id) {
        if (id != null && documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            log.info("Document entity {} deleted from database", id);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return id != null && documentRepository.existsById(id);
    }
}
