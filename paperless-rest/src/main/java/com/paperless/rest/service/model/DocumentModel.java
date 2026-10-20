package com.paperless.rest.service.model;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.InvalidStatusTransitionException;
import com.paperless.rest.service.validation.annotation.ValidFilename;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure Business Layer (BL) Domain Model.
 * Decoupled from both the presentation tier (DTO) and the persistence tier (DAL Entity).
 * Contains declarative validation annotations validated by DocumentModelValidator.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModel {

    private Long id;
    private Long version;

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
            throw new InvalidStatusTransitionException(
                    "Illegal status transition from " + this.currentStatus + " to " + targetStatus);
        }
        this.currentStatus = targetStatus;
    }
}
