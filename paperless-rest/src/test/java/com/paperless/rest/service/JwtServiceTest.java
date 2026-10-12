package com.paperless.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("unit-test-secret-key-at-least-32-chars-long!", 3600000L);
    }

    // GIVEN ein gültiger Benutzername und eine Rolle
    // WHEN ein JWT-Token generiert wird
    // THEN ist das Token syntaktisch gültig und die Validierung ist erfolgreich
    @Test
    void generateToken_WithValidUser_GeneratesValidJwt() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    // GIVEN ein manipuliertes JWT-Token mit veränderter Signatur
    // WHEN das Token validiert wird
    // THEN schlägt die Validierung fehl und liefert false
    @Test
    void validateToken_WithTamperedSignature_ReturnsFalse() {
        String token = jwtService.generateToken("user", "USER");
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidSignature";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    // GIVEN ein ungültiges oder leeres Token
    // WHEN validateToken aufgerufen wird
    // THEN liefert die Validierung false zurück
    @Test
    void validateToken_WithNullOrBlankToken_ReturnsFalse() {
        assertFalse(jwtService.validateToken(null));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken("not.a.valid.jwt.token"));
    }

    // GIVEN ein abgelaufenes JWT-Token
    // WHEN die Gültigkeit geprüft wird
    // THEN wird das Token als ungültig erkannt
    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // Negative expiration to simulate already expired token
        JwtService expiredJwtService = new JwtService("unit-test-secret-key-at-least-32-chars-long!", -1000L);
        String expiredToken = expiredJwtService.generateToken("user", "USER");

        assertFalse(jwtService.validateToken(expiredToken));
    }
}
