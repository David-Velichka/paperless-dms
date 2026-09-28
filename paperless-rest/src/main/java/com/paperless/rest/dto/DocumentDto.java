package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;

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
