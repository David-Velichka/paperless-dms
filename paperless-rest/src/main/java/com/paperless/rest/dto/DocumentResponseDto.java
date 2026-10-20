package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Query response DTO representing complete document metadata returned to clients.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {

    private Long id;
    private Long version;
    private String title;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String storagePath;
    private String ocrText;
    private String summary;
    private DocumentStatus currentStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime modifiedAt;
}
