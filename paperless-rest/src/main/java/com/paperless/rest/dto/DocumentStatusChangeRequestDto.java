package com.paperless.rest.dto;

import com.paperless.rest.service.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for requesting a document status transition.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusChangeRequestDto {

    @NotNull(message = "Target status must not be null")
    private DocumentStatus status;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}
