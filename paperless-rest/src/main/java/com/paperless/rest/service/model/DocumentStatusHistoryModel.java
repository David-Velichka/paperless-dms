package com.paperless.rest.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure Business Layer Domain Model for document status history audit log.
 * Enforces validation annotations verified by DocumentStatusHistoryModelValidator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusHistoryModel {

    private Long id;

    @NotNull(message = "Document id must not be null")
    private Long documentId;

    @NotNull(message = "Status must not be null")
    private DocumentStatus status;

    private DocumentStatus previousStatus;

    @NotBlank(message = "Changed by actor must not be blank")
    @Size(max = 100, message = "Changed by actor must not exceed 100 characters")
    private String changedBy;

    @NotNull(message = "Timestamp must not be null")
    private OffsetDateTime timestamp;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public DocumentStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(DocumentStatus previousStatus) { this.previousStatus = previousStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
