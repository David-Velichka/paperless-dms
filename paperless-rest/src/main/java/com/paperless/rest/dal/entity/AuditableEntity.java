package com.paperless.rest.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Base class for entities with audit timestamps (created and last modified, both in UTC).
 */
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    // The callbacks have their own names, so a callback in a subclass does not replace them
    @PrePersist
    protected void onAuditCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        modifiedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onAuditUpdate() {
        modifiedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
