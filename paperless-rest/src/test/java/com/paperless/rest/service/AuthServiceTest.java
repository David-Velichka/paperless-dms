package com.paperless.rest.service;

import com.paperless.rest.dto.LoginRequestDto;
import com.paperless.rest.dto.LoginResponseDto;
import com.paperless.rest.exception.InvalidCredentialsException;
import com.paperless.rest.service.impl.AuthServiceImpl;
import com.paperless.rest.service.model.UserModel;
import com.paperless.rest.service.validation.UserModelValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserModelValidator userModelValidator;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(jwtService, userModelValidator);
    }

    // GIVEN gültige Anmeldedaten für einen Benutzer
    // WHEN die login-Methode aufgerufen wird
    // THEN wird ein erfolgreiches LoginResponseDto mit Token und Benutzerdaten zurückgegeben
    @Test
    void login_WithValidCredentials_ReturnsLoginResponse() {
        LoginRequestDto request = new LoginRequestDto("admin", "admin123");

        doNothing().when(userModelValidator).validateOrThrow(any(UserModel.class));
        when(jwtService.generateToken("admin", "ADMIN")).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());
        assertEquals(3600000L, response.getExpiresInMs());
        verify(userModelValidator, times(1)).validateOrThrow(any(UserModel.class));
    }

    // GIVEN ein falsches Passwort für einen bestehenden Benutzer
    // WHEN die login-Methode aufgerufen wird
    // THEN wird eine InvalidCredentialsException geworfen
    @Test
    void login_WithInvalidPassword_ThrowsInvalidCredentialsException() {
        LoginRequestDto request = new LoginRequestDto("admin", "wrongpassword");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(userModelValidator, never()).validateOrThrow(any());
    }

    // GIVEN ein unbekannter Benutzername
    // WHEN die login-Methode aufgerufen wird
    // THEN wird eine InvalidCredentialsException geworfen
    @Test
    void login_WithUnknownUser_ThrowsInvalidCredentialsException() {
        LoginRequestDto request = new LoginRequestDto("nonexistent", "secret");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(userModelValidator, never()).validateOrThrow(any());
    }

    // GIVEN ein Null-Login-Request
    // WHEN die login-Methode aufgerufen wird
    // THEN wird eine InvalidCredentialsException geworfen
    @Test
    void login_WithNullRequest_ThrowsInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login(null));
        verify(userModelValidator, never()).validateOrThrow(any());
    }

    // GIVEN ein gültiges Token
    // WHEN getCurrentUser aufgerufen wird
    // THEN wird das zugehörige UserModel validiert und zurückgegeben
    @Test
    void getCurrentUser_WithValidToken_ReturnsUserModel() {
        String token = "valid.token";
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user");
        when(jwtService.extractRole(token)).thenReturn("USER");
        doNothing().when(userModelValidator).validateOrThrow(any(UserModel.class));

        UserModel user = authService.getCurrentUser(token);

        assertNotNull(user);
        assertEquals("user", user.getUsername());
        assertEquals("USER", user.getRole());
        verify(userModelValidator, times(1)).validateOrThrow(any(UserModel.class));
    }

    // GIVEN ein ungültiges Token
    // WHEN getCurrentUser aufgerufen wird
    // THEN wird eine InvalidCredentialsException geworfen
    @Test
    void getCurrentUser_WithInvalidToken_ThrowsInvalidCredentialsException() {
        String token = "invalid.token";
        when(jwtService.validateToken(token)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.getCurrentUser(token));
        verify(userModelValidator, never()).validateOrThrow(any());
    }

    // GIVEN ein Null-Token
    // WHEN getCurrentUser aufgerufen wird
    // THEN wird eine InvalidCredentialsException geworfen
    @Test
    void getCurrentUser_WithNullToken_ThrowsInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class, () -> authService.getCurrentUser(null));
        verify(userModelValidator, never()).validateOrThrow(any());
    }
}
