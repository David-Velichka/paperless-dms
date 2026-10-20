package com.paperless.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validated query parameter object for document search requests.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchQueryDto {

    @NotBlank(message = "Search query parameter must not be blank")
    @Size(min = 1, max = 100, message = "Search query parameter must be between 1 and 100 characters")
    private String query;
}
