package com.paperless.rest.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pure Business Layer Domain Model for User.
 * Enforces validation annotations verified by UserModelValidator.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    @NotBlank(message = "Username must not be blank")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @NotBlank(message = "Role must not be blank")
    @Size(max = 20, message = "Role must not exceed 20 characters")
    private String role;
}
