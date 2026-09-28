package com.paperless.rest.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure Business Layer Domain Model for document status history audit log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusHistoryModel {
    private Long id;
    private Long documentId;
    private DocumentStatus status;
    private DocumentStatus previousStatus;
    private String changedBy;
    private OffsetDateTime timestamp;
    private String comment;
}
