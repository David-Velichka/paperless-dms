package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO representing an entry in the document status history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusHistoryDto {

    private Long id;
    private Long documentId;
    private DocumentStatus status;
    private DocumentStatus previousStatus;
    private String changedBy;
    private OffsetDateTime timestamp;
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
