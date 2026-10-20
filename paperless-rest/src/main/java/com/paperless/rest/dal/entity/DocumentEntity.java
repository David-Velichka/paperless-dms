package com.paperless.rest.dal.entity;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Data Access Layer (DAL) Entity for PostgreSQL persistence.
 */
@Entity
@Table(name = "documents")
@EntityListeners(DocumentEntityListener.class)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Setter
    @Column(nullable = false)
    private String title;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "storage_path")
    private String storagePath;

    @Setter
    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Setter
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 50)
    private DocumentStatus currentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (currentStatus == null) {
            currentStatus = DocumentStatus.RECEIVED;
        }
        modifiedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentEntity that = (DocumentEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
