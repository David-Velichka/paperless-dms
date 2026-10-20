package com.paperless.rest.service.impl;

import com.paperless.rest.dto.LoginRequestDto;
import com.paperless.rest.dto.LoginResponseDto;
import com.paperless.rest.exception.InvalidCredentialsException;
import com.paperless.rest.service.AuthService;
import com.paperless.rest.service.JwtService;
import com.paperless.rest.service.model.UserModel;
import com.paperless.rest.service.validation.UserModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserModelValidator userModelValidator;

    // Student demo credentials
    private static final Map<String, String> USERS = Map.of(
            "admin", "admin123",
            "user", "password"
    );

    private static final Map<String, String> ROLES = Map.of(
            "admin", "ADMIN",
            "user", "USER"
    );

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new InvalidCredentialsException("Username and password must not be null");
        }
        String expectedPassword = USERS.get(request.getUsername());
        if (expectedPassword == null || !expectedPassword.equals(request.getPassword())) {
            log.warn("Failed authentication attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String role = ROLES.getOrDefault(request.getUsername(), "USER");
        UserModel userModel = new UserModel(request.getUsername(), role);
        userModelValidator.validateOrThrow(userModel);

        String token = jwtService.generateToken(request.getUsername(), role);

        log.info("User {} successfully authenticated with role {}", request.getUsername(), role);
        return new LoginResponseDto(token, request.getUsername(), role, jwtService.getExpirationMs());
    }

    @Override
    public UserModel getCurrentUser(String token) {
        if (token == null || token.isBlank() || !jwtService.validateToken(token)) {
            throw new InvalidCredentialsException("Invalid or expired authentication token");
        }
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        UserModel userModel = new UserModel(username, role);
        userModelValidator.validateOrThrow(userModel);
        return userModel;
    }
}
