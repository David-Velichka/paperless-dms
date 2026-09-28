package com.paperless.rest.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Pure Business Layer (BL) Domain Model.
 * Decoupled from both the presentation tier (DTO) and the persistence tier (DAL Entity).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModel {
    private Long id;
    private String title;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String storagePath;
    private String ocrText;
    private String summary;
    private DocumentStatus currentStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;

    /**
     * Enforces domain invariant for status transitions (Aggregate Root self-management).
     *
     * @param targetStatus the desired target lifecycle state
     * @throws com.paperless.rest.exception.BusinessLayerException if the transition is prohibited
     */
    public void transitionTo(DocumentStatus targetStatus) {
        if (targetStatus == null) {
            throw new com.paperless.rest.exception.BusinessLayerException("Target status must not be null");
        }
        if (this.currentStatus == null) {
            this.currentStatus = DocumentStatus.RECEIVED;
        }
        if (!this.currentStatus.canTransitionTo(targetStatus)) {
            throw new com.paperless.rest.exception.BusinessLayerException(
                    "Illegal status transition from " + this.currentStatus + " to " + targetStatus);
        }
        this.currentStatus = targetStatus;
    }
}
