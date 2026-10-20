package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Legacy/composite Document DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private Long id;
    private Long version;

    @NotBlank(message = "Title must not be blank")
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
