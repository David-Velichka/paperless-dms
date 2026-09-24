package com.paperless.rest.controller;

import com.paperless.rest.dto.DocumentDto;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.model.DocumentModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentMapper documentMapper;

    // GIVEN eine Anfrage zum Abrufen aller Dokumente
    // WHEN der Endpunkt GET /api/documents aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und ein JSON-Array der Dokumente zurückgegeben
    @Test
    void getAllDocuments_ReturnsOkAndJsonArray() throws Exception {
        DocumentModel model = DocumentModel.builder().id(1L).title("Sample").build();
        DocumentDto dto = DocumentDto.builder().id(1L).title("Sample").build();

        when(documentService.findAll()).thenReturn(List.of(model));
        when(documentMapper.toDtoList(List.of(model))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample"));
    }

    // GIVEN eine gültige PDF-Datei und ein optionaler Titel
    // WHEN POST /api/documents/upload mit Multipart-Formulardaten aufgerufen wird
    // THEN wird der HTTP-Status 201 Created und das erstellte DocumentDto zurückgegeben
    @Test
    void uploadDocument_WithValidFile_ReturnsCreated() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "sample.pdf", "application/pdf", "Dummy PDF content".getBytes());

        DocumentModel savedModel = DocumentModel.builder()
                .id(42L)
                .title("Custom Title")
                .originalFilename("sample.pdf")
                .contentType("application/pdf")
                .fileSize(17L)
                .build();

        DocumentDto savedDto = DocumentDto.builder()
                .id(42L)
                .title("Custom Title")
                .originalFilename("sample.pdf")
                .contentType("application/pdf")
                .fileSize(17L)
                .build();

        when(documentService.uploadDocument(org.mockito.ArgumentMatchers.any(DocumentModel.class), org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenReturn(savedModel);
        when(documentMapper.toDto(savedModel)).thenReturn(savedDto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Custom Title"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.title").value("Custom Title"))
                .andExpect(jsonPath("$.originalFilename").value("sample.pdf"));
    }

    // GIVEN eine leere Datei im Upload-Request
    // WHEN POST /api/documents/upload aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void uploadDocument_WithEmptyFile_ReturnsBadRequest() throws Exception {
        org.springframework.mock.web.MockMultipartFile emptyFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/documents/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest());
    }
}
