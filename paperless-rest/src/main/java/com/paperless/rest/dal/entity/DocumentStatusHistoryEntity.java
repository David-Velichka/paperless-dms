package com.paperless.rest.dal.entity;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Data Access Layer (DAL) Entity for document status history in PostgreSQL persistence.
 */
@Entity
@Table(name = "document_status_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private DocumentStatus previousStatus;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime timestamp;

    @Column(length = 500)
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

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }
}
