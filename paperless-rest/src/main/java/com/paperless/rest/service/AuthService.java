package com.paperless.rest.service;

import com.paperless.rest.dto.LoginRequestDto;
import com.paperless.rest.dto.LoginResponseDto;
import com.paperless.rest.service.model.UserModel;

public interface AuthService {

    /**
     * Authenticates user credentials and generates a signed JWT access token.
     */
    LoginResponseDto login(LoginRequestDto request);

    /**
     * Retrieves the authenticated user details from a valid JWT access token.
     */
    UserModel getCurrentUser(String token);
}
