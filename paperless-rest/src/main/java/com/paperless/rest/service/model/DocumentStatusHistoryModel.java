package com.paperless.rest.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure Business Layer Domain Model for document status history audit log.
 * Enforces validation annotations verified by DocumentStatusHistoryModelValidator.
 */
@Getter
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

    @NotNull(message = "Timestamp must not be null")
    private OffsetDateTime timestamp;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}
