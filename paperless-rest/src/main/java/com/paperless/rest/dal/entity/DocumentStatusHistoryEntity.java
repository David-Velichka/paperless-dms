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

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }
}
