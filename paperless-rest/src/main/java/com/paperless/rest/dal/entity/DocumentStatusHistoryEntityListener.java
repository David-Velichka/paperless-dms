package com.paperless.rest.dal.entity;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import lombok.extern.slf4j.Slf4j;

/**
 * Entity lifecycle listener for Data Access Layer (DAL) logging.
 * Logs append-only status transition events for DocumentStatusHistoryEntity.
 */
@Slf4j
public class DocumentStatusHistoryEntityListener {

    @PostPersist
    public void onPostPersist(DocumentStatusHistoryEntity entity) {
        log.debug("DAL persistence event: DocumentStatusHistory recorded for documentId={}, transition={} -> {}, changedBy='{}'",
                entity.getDocumentId(), entity.getPreviousStatus(), entity.getStatus(), entity.getChangedBy());
    }

    @PostRemove
    public void onPostRemove(DocumentStatusHistoryEntity entity) {
        log.debug("DAL persistence event: DocumentStatusHistory removed with id={}", entity.getId());
    }
}

