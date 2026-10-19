package com.paperless.rest.service.model;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.validation.annotation.ValidFilename;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure Business Layer (BL) Domain Model.
 * Decoupled from both the presentation tier (DTO) and the persistence tier (DAL Entity).
 * Contains declarative validation annotations validated by DocumentModelValidator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModel {

    private Long id;

    @NotBlank(message = "Document title must not be blank")
    @Size(max = 255, message = "Document title must not exceed 255 characters")
    private String title;

    @ValidFilename
    @Size(max = 255, message = "Original filename must not exceed 255 characters")
    private String originalFilename;

    @Size(max = 100, message = "Content type must not exceed 100 characters")
    private String contentType;

    @PositiveOrZero(message = "File size must not be negative")
    private Long fileSize;

    @Size(max = 1000, message = "Storage path must not exceed 1000 characters")
    private String storagePath;

    private String ocrText;

    private String summary;

    private DocumentStatus currentStatus;

    private OffsetDateTime createdAt;

    private OffsetDateTime modifiedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public DocumentStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(DocumentStatus currentStatus) { this.currentStatus = currentStatus; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(OffsetDateTime modifiedAt) { this.modifiedAt = modifiedAt; }

    /**
     * Enforces domain invariant for status transitions (Aggregate Root self-management).
     *
     * @param targetStatus the desired target lifecycle state
     * @throws BusinessLayerException if the transition is prohibited
     */
    public void transitionTo(DocumentStatus targetStatus) {
        if (targetStatus == null) {
            throw new BusinessLayerException("Target status must not be null");
        }
        if (this.currentStatus == null) {
            this.currentStatus = DocumentStatus.RECEIVED;
        }
        if (!this.currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessLayerException(
                    "Illegal status transition from " + this.currentStatus + " to " + targetStatus);
        }
        this.currentStatus = targetStatus;
    }
}
