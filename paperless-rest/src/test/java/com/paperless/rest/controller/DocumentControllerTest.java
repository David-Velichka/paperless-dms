package com.paperless.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperless.rest.dto.DocumentDto;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.model.DocumentModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private DocumentMapper documentMapper;

    // --- Action 1: getAllDocuments ---

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

    // GIVEN ein Fehler beim Abrufen der Dokumente in der Geschäftslogik
    // WHEN GET /api/documents aufgerufen wird und der Service fehlschlägt
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void getAllDocuments_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(documentService.findAll()).thenThrow(new BusinessLayerException("Database connection failure"));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Database connection failure"));
    }

    // --- Action 2: getDocumentById ---

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN GET /api/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und das Dokument-DTO zurückgegeben
    @Test
    void getDocumentById_WithValidId_ReturnsDocumentDto() throws Exception {
        DocumentModel model = DocumentModel.builder().id(10L).title("Document 10").build();
        DocumentDto dto = DocumentDto.builder().id(10L).title("Document 10").build();

        when(documentService.findById(10L)).thenReturn(model);
        when(documentMapper.toDto(model)).thenReturn(dto);

        mockMvc.perform(get("/api/documents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Document 10"));
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN GET /api/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void getDocumentById_WithNonExistingId_ReturnsBadRequest() throws Exception {
        when(documentService.findById(999L)).thenThrow(new BusinessLayerException("Document with id 999 not found"));

        mockMvc.perform(get("/api/documents/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Document with id 999 not found"));
    }

    // --- Action 3: searchDocuments ---

    // GIVEN ein gültiger Suchbegriff
    // WHEN GET /api/documents/search mit Abfrageparameter aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und die Ergebnisliste zurückgegeben
    @Test
    void searchDocuments_WithValidQuery_ReturnsMatchingDocuments() throws Exception {
        DocumentModel model = DocumentModel.builder().id(1L).title("Financial Report").build();
        DocumentDto dto = DocumentDto.builder().id(1L).title("Financial Report").build();

        when(documentService.search("Report")).thenReturn(List.of(model));
        when(documentMapper.toDtoList(List.of(model))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/documents/search").param("query", "Report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Financial Report"));
    }

    // GIVEN ein Fehler bei der Dokumentensuche in der Geschäftslogik
    // WHEN GET /api/documents/search aufgerufen wird und der Service fehlschlägt
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void searchDocuments_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(documentService.search("bad_query")).thenThrow(new BusinessLayerException("Search query syntax invalid"));

        mockMvc.perform(get("/api/documents/search").param("query", "bad_query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Search query syntax invalid"));
    }

    // --- Action 4: createDocument ---

    // GIVEN ein gültiges DocumentDto im Request-Body
    // WHEN POST /api/documents aufgerufen wird
    // THEN wird der HTTP-Status 201 Created und das erstellte DTO zurückgegeben
    @Test
    void createDocument_WithValidDto_ReturnsCreated() throws Exception {
        DocumentDto requestDto = DocumentDto.builder().title("New Invoice").build();
        DocumentModel model = DocumentModel.builder().title("New Invoice").build();
        DocumentModel savedModel = DocumentModel.builder().id(5L).title("New Invoice").build();
        DocumentDto savedDto = DocumentDto.builder().id(5L).title("New Invoice").build();

        when(documentMapper.toModel(any(DocumentDto.class))).thenReturn(model);
        when(documentService.save(model)).thenReturn(savedModel);
        when(documentMapper.toDto(savedModel)).thenReturn(savedDto);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("New Invoice"));
    }

    // GIVEN ein DocumentDto mit leerem Titel
    // WHEN POST /api/documents aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request wegen Validierungsfehlern zurückgegeben
    @Test
    void createDocument_WithBlankTitle_ReturnsBadRequest() throws Exception {
        DocumentDto invalidDto = DocumentDto.builder().title("   ").build();

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // --- Action 5: uploadDocument ---

    // GIVEN eine gültige PDF-Datei und ein optionaler Titel
    // WHEN POST /api/documents/upload mit Multipart-Formulardaten aufgerufen wird
    // THEN wird der HTTP-Status 201 Created und das erstellte DocumentDto zurückgegeben
    @Test
    void uploadDocument_WithValidFile_ReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
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

        when(documentService.uploadDocument(any(DocumentModel.class), any(byte[].class)))
                .thenReturn(savedModel);
        when(documentMapper.toDto(savedModel)).thenReturn(savedDto);

        mockMvc.perform(multipart("/api/documents/upload")
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
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    // --- Action 6: deleteDocument ---

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN DELETE /api/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 204 No Content zurückgegeben
    @Test
    void deleteDocument_WithValidId_ReturnsNoContent() throws Exception {
        doNothing().when(documentService).deleteById(1L);

        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN DELETE /api/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void deleteDocument_WithNonExistingId_ReturnsBadRequest() throws Exception {
        doThrow(new BusinessLayerException("Cannot delete: Document with id 99 does not exist"))
                .when(documentService).deleteById(99L);

        mockMvc.perform(delete("/api/documents/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot delete: Document with id 99 does not exist"));
    }

    // GIVEN ein nebenläufiger Änderungskonflikt (Optimistic Locking Failure)
    // WHEN POST /api/documents aufgerufen wird und die DB einen Versionskonflikt meldet
    // THEN wird der HTTP-Status 409 Conflict und eine aussagekräftige Fehlermeldung zurückgegeben
    @Test
    void createDocument_WhenConcurrentConflictOccurs_ReturnsConflict() throws Exception {
        DocumentDto requestDto = DocumentDto.builder().title("Concurrent Invoice").build();
        DocumentModel model = DocumentModel.builder().title("Concurrent Invoice").build();

        when(documentMapper.toModel(any(DocumentDto.class))).thenReturn(model);
        when(documentService.save(any(DocumentModel.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(DocumentModel.class, 1L));

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The document was modified concurrently by another transaction. Please refresh and try again."));
    }
}
