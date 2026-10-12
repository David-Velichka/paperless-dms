package com.paperless.rest.controller;

import com.paperless.rest.config.JwtAuthFilter;
import com.paperless.rest.config.SecurityFilterConfig;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({SecurityFilterConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "security.jwt.enabled=true",
        "jwt.secret=integration-test-secret-key-at-least-32-chars-long!",
        "jwt.expiration-ms=3600000"
})
class JwtAuthFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentMapper documentMapper;

    // GIVEN ein geschützter Endpunkt ohne Authorization-Header
    // WHEN GET /api/documents aufgerufen wird
    // THEN wird der Zugriff mit HTTP-Status 401 Unauthorized verweigert
    @Test
    void getDocuments_WithoutAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    // GIVEN ein ungültiges oder manipuliertes Bearer-Token
    // WHEN GET /api/documents aufgerufen wird
    // THEN wird der Zugriff mit HTTP-Status 401 Unauthorized verweigert
    @Test
    void getDocuments_WithInvalidBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer invalid.fake.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired JWT token"));
    }

    // GIVEN ein kryptografisch gültiges JWT-Bearer-Token
    // WHEN GET /api/documents aufgerufen wird
    // THEN gewährt der Filter Zugriff und liefert HTTP-Status 200 OK
    @Test
    void getDocuments_WithValidBearerToken_ReturnsOk() throws Exception {
        String token = jwtService.generateToken("admin", "ADMIN");
        when(documentService.findAll()).thenReturn(Collections.emptyList());
        when(documentMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
