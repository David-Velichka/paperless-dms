package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting a document status transition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusChangeRequestDto {

    @NotNull(message = "Target status must not be null")
    private DocumentStatus status;

    @NotBlank(message = "Changed by actor must not be blank")
    @Size(max = 100, message = "Changed by actor must not exceed 100 characters")
    private String changedBy;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}
