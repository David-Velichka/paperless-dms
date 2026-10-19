package com.paperless.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperless.rest.dto.LoginRequestDto;
import com.paperless.rest.dto.LoginResponseDto;
import com.paperless.rest.dto.UserDto;
import com.paperless.rest.exception.InvalidCredentialsException;
import com.paperless.rest.mapper.UserMapper;
import com.paperless.rest.service.AuthService;
import com.paperless.rest.service.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    // --- Action 1: login ---

    // GIVEN gültige Anmeldedaten im Request-Body
    // WHEN POST /api/auth/login aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und das LoginResponseDto mit Token zurückgegeben
    @Test
    void login_WithValidCredentials_ReturnsOkAndToken() throws Exception {
        LoginRequestDto request = new LoginRequestDto("admin", "admin123");
        LoginResponseDto response = new LoginResponseDto("jwt.token.sample", "admin", "ADMIN", 86400000L);

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.sample"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // GIVEN ungültige Anmeldedaten
    // WHEN POST /api/auth/login aufgerufen wird
    // THEN wird der HTTP-Status 401 Unauthorized zurückgegeben
    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequestDto request = new LoginRequestDto("wrong", "wrong");

        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // GIVEN ein Login-Request mit leerem Benutzernamen
    // WHEN POST /api/auth/login aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request wegen Validierungsfehlern zurückgegeben
    @Test
    void login_WithBlankUsername_ReturnsBadRequest() throws Exception {
        LoginRequestDto request = new LoginRequestDto("   ", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // --- Action 2: getCurrentUser ---

    // GIVEN ein gültiger Bearer-Header im Authorization-Header
    // WHEN GET /api/auth/me aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und das UserDto zurückgegeben
    @Test
    void getCurrentUser_WithValidBearerToken_ReturnsOk() throws Exception {
        UserModel userModel = new UserModel("admin", "ADMIN");
        UserDto userDto = new UserDto("admin", "ADMIN");

        when(authService.getCurrentUser("valid.token")).thenReturn(userModel);
        when(userMapper.toDto(userModel)).thenReturn(userDto);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // GIVEN ein fehlender Authorization-Header
    // WHEN GET /api/auth/me aufgerufen wird
    // THEN wird der HTTP-Status 401 Unauthorized zurückgegeben
    @Test
    void getCurrentUser_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // GIVEN ein ungültiger oder abgelaufener Authentifizierungs-Token
    // WHEN GET /api/auth/me aufgerufen wird
    // THEN liefert der Service InvalidCredentialsException und der Controller 401 Unauthorized
    @Test
    void getCurrentUser_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        when(authService.getCurrentUser("invalid.token"))
                .thenThrow(new InvalidCredentialsException("Invalid or expired authentication token"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired authentication token"));
    }
}
