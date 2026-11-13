package com.paperless.rest.dal.entity;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Data Access Layer (DAL) Entity for PostgreSQL persistence.
 */
@Entity
@Table(name = "documents")
@EntityListeners(DocumentEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity extends AuditableEntity {

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

    // createdAt and modifiedAt come from AuditableEntity
    @PrePersist
    protected void onCreate() {
        if (currentStatus == null) {
            currentStatus = DocumentStatus.RECEIVED;
        }
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
