package com.paperless.rest.dal.entity;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Entity lifecycle listener for Data Access Layer (DAL) logging.
 * Logs persistence lifecycle events (create, update, remove) for DocumentEntity.
 */
@Slf4j
public class DocumentEntityListener {

    @PostPersist
    public void onPostPersist(DocumentEntity entity) {
        log.debug("DAL persistence event: DocumentEntity persisted with id={}, title='{}'", entity.getId(), entity.getTitle());
    }

    @PostUpdate
    public void onPostUpdate(DocumentEntity entity) {
        log.debug("DAL persistence event: DocumentEntity updated with id={}, status={}, version={}",
                entity.getId(), entity.getCurrentStatus(), entity.getVersion());
    }

    @PostRemove
    public void onPostRemove(DocumentEntity entity) {
        log.debug("DAL persistence event: DocumentEntity removed with id={}", entity.getId());
    }
}

