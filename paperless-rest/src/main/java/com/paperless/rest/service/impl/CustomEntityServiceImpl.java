package com.paperless.rest.service.impl;

import com.paperless.rest.dal.entity.CustomEntity;
import com.paperless.rest.dal.repository.CustomEntityRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.CustomEntityMapper;
import com.paperless.rest.service.CustomEntityService;
import com.paperless.rest.service.model.CustomEntityModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomEntityServiceImpl implements CustomEntityService {

    private final CustomEntityRepository customEntityRepository;
    private final CustomEntityMapper customEntityMapper;

    @Override
    public CustomEntityModel create(CustomEntityModel model) {
        if (model == null) {
            throw new BusinessLayerException("Custom entity model must not be null");
        }
        if (model.getName() == null || model.getName().isBlank()) {
            throw new BusinessLayerException("Custom entity name must not be blank");
        }
        log.info("Creating custom entity: {}", model.getName());
        CustomEntity entity = customEntityMapper.toEntity(model);
        CustomEntity saved = customEntityRepository.save(entity);
        return customEntityMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomEntityModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Custom entity id must not be null");
        }
        log.debug("Finding custom entity by id: {}", id);
        return customEntityRepository.findById(id)
                .map(customEntityMapper::toModel)
                .orElseThrow(() -> new BusinessLayerException("CustomEntity with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomEntityModel> findByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        log.debug("Finding custom entities by documentId: {}", documentId);
        List<CustomEntity> entities = customEntityRepository.findByDocumentId(documentId);
        return customEntityMapper.toModelList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomEntityModel> findAll() {
        return customEntityMapper.toModelList(customEntityRepository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("Custom entity id must not be null");
        }
        log.info("Deleting custom entity by id: {}", id);
        if (!customEntityRepository.existsById(id)) {
            throw new BusinessLayerException("CustomEntity with id " + id + " does not exist");
        }
        customEntityRepository.deleteById(id);
    }
}
