package com.paperless.rest.service.impl;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.mapper.DocumentEntityMapper;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.DocumentStatusHistoryMapper;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import com.paperless.rest.service.validation.DocumentStatusHistoryModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentStatusHistoryServiceImpl implements DocumentStatusHistoryService {

    private final DocumentRepository documentRepository;
    private final DocumentStatusHistoryRepository documentStatusHistoryRepository;
    private final DocumentEntityMapper documentEntityMapper;
    private final DocumentStatusHistoryMapper documentStatusHistoryMapper;
    private final DocumentStatusHistoryModelValidator historyModelValidator;

    @Override
    public DocumentStatusHistoryModel changeStatus(Long documentId, DocumentStatus targetStatus, String changedBy, String comment) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        if (targetStatus == null) {
            throw new BusinessLayerException("Target status must not be null");
        }

        DocumentEntity documentEntity = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessLayerException("Document with id " + documentId + " not found"));

        DocumentModel documentModel = documentEntityMapper.toModel(documentEntity);
        DocumentStatus previousStatus = documentModel.getCurrentStatus() != null ? documentModel.getCurrentStatus() : DocumentStatus.RECEIVED;

        log.info("Transitioning document {} status from {} to {} by {}", documentId, previousStatus, targetStatus, changedBy);

        documentModel.transitionTo(targetStatus);

        DocumentStatusHistoryModel historyModel = DocumentStatusHistoryModel.builder()
                .documentId(documentId)
                .status(targetStatus)
                .previousStatus(previousStatus)
                .changedBy(changedBy != null ? changedBy.trim() : null)
                .timestamp(OffsetDateTime.now())
                .comment(comment != null ? comment.trim() : null)
                .build();

        historyModelValidator.validateOrThrow(historyModel);

        documentEntity.setCurrentStatus(documentModel.getCurrentStatus());
        documentRepository.save(documentEntity);

        DocumentStatusHistoryEntity historyEntity = documentStatusHistoryMapper.toEntity(historyModel);
        DocumentStatusHistoryEntity savedHistory = documentStatusHistoryRepository.save(historyEntity);
        return documentStatusHistoryMapper.toModel(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentStatusHistoryModel> getHistoryByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        if (!documentRepository.existsById(documentId)) {
            throw new BusinessLayerException("Document with id " + documentId + " not found");
        }
        log.debug("Fetching status history for document id: {}", documentId);
        List<DocumentStatusHistoryEntity> entities = documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(documentId);
        return documentStatusHistoryMapper.toModelList(entities);
    }

    @Override
    public DocumentStatusHistoryModel createInitialHistory(Long documentId, String changedBy, String comment) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        String actor = (changedBy != null && !changedBy.isBlank()) ? changedBy.trim() : "uploader";
        String note = (comment != null && !comment.isBlank()) ? comment.trim() : "Initial upload";

        DocumentStatusHistoryModel historyModel = DocumentStatusHistoryModel.builder()
                .documentId(documentId)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .changedBy(actor)
                .timestamp(OffsetDateTime.now())
                .comment(note)
                .build();

        historyModelValidator.validateOrThrow(historyModel);

        DocumentStatusHistoryEntity historyEntity = documentStatusHistoryMapper.toEntity(historyModel);
        DocumentStatusHistoryEntity saved = documentStatusHistoryRepository.save(historyEntity);
        return documentStatusHistoryMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentStatusHistoryModel findById(Long id) {
        if (id == null) {
            throw new BusinessLayerException("History id must not be null");
        }
        return documentStatusHistoryRepository.findById(id)
                .map(documentStatusHistoryMapper::toModel)
                .orElseThrow(() -> new BusinessLayerException("DocumentStatusHistory with id " + id + " not found"));
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessLayerException("Document id must not be null");
        }
        documentStatusHistoryRepository.deleteByDocumentId(documentId);
    }
}
