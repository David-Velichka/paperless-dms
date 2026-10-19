package com.paperless.rest.controller;

import com.paperless.rest.dto.LoginRequestDto;
import com.paperless.rest.dto.LoginResponseDto;
import com.paperless.rest.dto.UserDto;
import com.paperless.rest.exception.InvalidCredentialsException;
import com.paperless.rest.mapper.UserMapper;
import com.paperless.rest.service.AuthService;
import com.paperless.rest.service.model.UserModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Received login request for user: {}", request.getUsername());
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            jakarta.servlet.http.HttpServletRequest request) {
        log.info("REST request to get current authenticated user");
        String authenticatedUser = (String) request.getAttribute("authenticatedUser");
        String authenticatedRole = (String) request.getAttribute("authenticatedRole");

        if (authenticatedUser != null) {
            return ResponseEntity.ok(new UserDto(authenticatedUser, authenticatedRole));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7).trim();
        UserModel userModel = authService.getCurrentUser(token);
        return ResponseEntity.ok(userMapper.toDto(userModel));
    }
}
