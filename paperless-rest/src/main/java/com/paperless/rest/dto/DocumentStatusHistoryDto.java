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
}
