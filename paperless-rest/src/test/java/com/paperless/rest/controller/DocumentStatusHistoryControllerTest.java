package com.paperless.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperless.rest.dto.DocumentStatusChangeRequestDto;
import com.paperless.rest.dto.DocumentStatusHistoryDto;
import com.paperless.rest.mapper.DocumentStatusHistoryMapper;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentStatusHistoryController.class)
class DocumentStatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DocumentStatusHistoryService documentStatusHistoryService;

    @MockitoBean
    private DocumentStatusHistoryMapper documentStatusHistoryMapper;

    // GIVEN eine gültige Dokumenten-ID mit vorhandenem Verlauf
    // WHEN GET /api/documents/{id}/history aufgerufen wird
    // THEN liefert der Controller 200 OK und ein JSON-Array
    @Test
    void getHistoryForDocument_ReturnsOkAndJsonArray() throws Exception {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .id(1L)
                .documentId(10L)
                .status(DocumentStatus.RECEIVED)
                .changedBy("user")
                .timestamp(OffsetDateTime.now())
                .build();

        DocumentStatusHistoryDto dto = DocumentStatusHistoryDto.builder()
                .id(1L)
                .documentId(10L)
                .status(DocumentStatus.RECEIVED)
                .changedBy("user")
                .timestamp(OffsetDateTime.now())
                .build();

        when(documentStatusHistoryService.getHistoryByDocumentId(10L)).thenReturn(List.of(model));
        when(documentStatusHistoryMapper.toDtoList(List.of(model))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/documents/10/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    // GIVEN eine gültige Statusänderungsanforderung
    // WHEN POST /api/documents/{id}/status aufgerufen wird
    // THEN liefert der Controller 201 Created und den erstellten Verlaufseintrag
    @Test
    void changeDocumentStatus_WithValidRequest_ReturnsCreated() throws Exception {
        DocumentStatusChangeRequestDto request = DocumentStatusChangeRequestDto.builder()
                .status(DocumentStatus.PROCESSING)
                .changedBy("worker")
                .comment("Starting OCR")
                .build();

        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .id(2L)
                .documentId(10L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("worker")
                .comment("Starting OCR")
                .build();

        DocumentStatusHistoryDto dto = DocumentStatusHistoryDto.builder()
                .id(2L)
                .documentId(10L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("worker")
                .comment("Starting OCR")
                .build();

        when(documentStatusHistoryService.changeStatus(eq(10L), eq(DocumentStatus.PROCESSING), eq("worker"), eq("Starting OCR")))
                .thenReturn(model);
        when(documentStatusHistoryMapper.toDto(model)).thenReturn(dto);

        mockMvc.perform(post("/api/documents/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    // GIVEN eine Statusänderungsanforderung ohne Zielstatus
    // WHEN POST /api/documents/{id}/status aufgerufen wird
    // THEN liefert der Controller 400 Bad Request wegen Validierungsfehler
    @Test
    void changeDocumentStatus_WithMissingStatus_ReturnsBadRequest() throws Exception {
        DocumentStatusChangeRequestDto request = DocumentStatusChangeRequestDto.builder()
                .status(null)
                .changedBy("worker")
                .build();

        mockMvc.perform(post("/api/documents/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // GIVEN eine Statusänderungsanforderung mit leerem Akteur
    // WHEN POST /api/documents/{id}/status aufgerufen wird
    // THEN liefert der Controller 400 Bad Request wegen Validierungsfehler
    @Test
    void changeDocumentStatus_WithBlankChangedBy_ReturnsBadRequest() throws Exception {
        DocumentStatusChangeRequestDto request = DocumentStatusChangeRequestDto.builder()
                .status(DocumentStatus.PROCESSING)
                .changedBy("   ")
                .build();

        mockMvc.perform(post("/api/documents/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // GIVEN eine gültige Verlaufs-ID
    // WHEN GET /api/documents/status-history/{id} aufgerufen wird
    // THEN liefert der Controller 200 OK und das DTO
    @Test
    void getHistoryById_ReturnsOk() throws Exception {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .id(5L)
                .documentId(10L)
                .status(DocumentStatus.REVIEW_PENDING)
                .changedBy("user")
                .build();

        DocumentStatusHistoryDto dto = DocumentStatusHistoryDto.builder()
                .id(5L)
                .documentId(10L)
                .status(DocumentStatus.REVIEW_PENDING)
                .changedBy("user")
                .build();

        when(documentStatusHistoryService.findById(5L)).thenReturn(model);
        when(documentStatusHistoryMapper.toDto(model)).thenReturn(dto);

        mockMvc.perform(get("/api/documents/status-history/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("REVIEW_PENDING"));
    }
}
