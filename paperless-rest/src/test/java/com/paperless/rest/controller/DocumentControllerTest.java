package com.paperless.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentResponseDto;
import com.paperless.rest.dto.DocumentUpdateDto;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.DocumentNotFoundException;
import com.paperless.rest.exception.MessageQueueException;
import com.paperless.rest.mapper.DocumentDtoMapper;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private DocumentDtoMapper documentDtoMapper;

    // --- Action 1: getAllDocuments ---

    // GIVEN eine Anfrage zum Abrufen aller Dokumente
    // WHEN der Endpunkt GET /api/v1/documents aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und ein JSON-Array der Dokumente zurückgegeben
    @Test
    void getAllDocuments_ReturnsOkAndJsonArray() throws Exception {
        DocumentModel model = DocumentModel.builder().id(1L).title("Sample").build();
        DocumentResponseDto dto = DocumentResponseDto.builder().id(1L).title("Sample").build();

        when(documentService.findAll()).thenReturn(List.of(model));
        when(documentDtoMapper.toResponseDtoList(List.of(model))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample"));
    }

    // GIVEN ein Fehler beim Abrufen der Dokumente in der Geschäftslogik
    // WHEN GET /api/v1/documents aufgerufen wird und der Service fehlschlägt
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void getAllDocuments_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(documentService.findAll()).thenThrow(new BusinessLayerException("Database connection failure"));

        mockMvc.perform(get("/api/v1/documents"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Database connection failure"))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:business-layer-error"));
    }

    // --- Action 2: getDocumentById ---

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN GET /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und das Dokument-DTO zurückgegeben
    @Test
    void getDocumentById_WithValidId_ReturnsDocumentResponseDto() throws Exception {
        DocumentModel model = DocumentModel.builder().id(10L).title("Document 10").build();
        DocumentResponseDto dto = DocumentResponseDto.builder().id(10L).title("Document 10").build();

        when(documentService.findById(10L)).thenReturn(model);
        when(documentDtoMapper.toResponseDto(model)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/documents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Document 10"));
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN GET /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void getDocumentById_WithNonExistingId_ReturnsBadRequest() throws Exception {
        when(documentService.findById(999L)).thenThrow(new BusinessLayerException("Document with id 999 not found"));

        mockMvc.perform(get("/api/v1/documents/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Document with id 999 not found"))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:business-layer-error"));
    }

    // --- Action 3: searchDocuments ---

    // GIVEN ein gültiger Suchbegriff
    // WHEN GET /api/v1/documents/search mit Abfrageparameter aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und die Ergebnisliste zurückgegeben
    @Test
    void searchDocuments_WithValidQuery_ReturnsMatchingDocuments() throws Exception {
        DocumentModel model = DocumentModel.builder().id(1L).title("Financial Report").build();
        DocumentResponseDto dto = DocumentResponseDto.builder().id(1L).title("Financial Report").build();

        when(documentService.search("Report")).thenReturn(List.of(model));
        when(documentDtoMapper.toResponseDtoList(List.of(model))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/documents/search").param("query", "Report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Financial Report"));
    }

    // GIVEN ein Fehler bei der Dokumentensuche in der Geschäftslogik
    // WHEN GET /api/v1/documents/search aufgerufen wird und der Service fehlschlägt
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void searchDocuments_WhenServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(documentService.search("bad_query")).thenThrow(new BusinessLayerException("Search query syntax invalid"));

        mockMvc.perform(get("/api/v1/documents/search").param("query", "bad_query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Search query syntax invalid"))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:business-layer-error"));
    }

    // GIVEN ein ungültiger Suchbegriff (leer)
    // WHEN GET /api/v1/documents/search mit leerem Query-Parameter aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request wegen Validierungsfehlern zurückgegeben
    @Test
    void searchDocuments_WithBlankQuery_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/documents/search").param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:validation-error"))
                .andExpect(jsonPath("$.invalidParams.query").exists());
    }

    // --- Action 4: createDocument ---

    // GIVEN ein gültiges DocumentCreateDto im Request-Body
    // WHEN POST /api/v1/documents aufgerufen wird
    // THEN wird der HTTP-Status 201 Created und das erstellte DTO zurückgegeben
    @Test
    void createDocument_WithValidDto_ReturnsCreated() throws Exception {
        DocumentCreateDto requestDto = DocumentCreateDto.builder().title("New Invoice").build();
        DocumentModel model = DocumentModel.builder().title("New Invoice").build();
        DocumentModel savedModel = DocumentModel.builder().id(5L).title("New Invoice").build();
        DocumentResponseDto savedDto = DocumentResponseDto.builder().id(5L).title("New Invoice").build();

        when(documentDtoMapper.toModel(any(DocumentCreateDto.class))).thenReturn(model);
        when(documentService.save(model)).thenReturn(savedModel);
        when(documentDtoMapper.toResponseDto(savedModel)).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("New Invoice"));
    }

    // GIVEN ein DocumentCreateDto mit leerem Titel
    // WHEN POST /api/v1/documents aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request wegen Validierungsfehlern zurückgegeben
    @Test
    void createDocument_WithBlankTitle_ReturnsBadRequest() throws Exception {
        DocumentCreateDto invalidDto = DocumentCreateDto.builder().title("   ").build();

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:validation-error"))
                .andExpect(jsonPath("$.invalidParams.title").exists());
    }

    // --- Action 5: uploadDocument ---

    // GIVEN eine gültige PDF-Datei und ein optionaler Titel
    // WHEN POST /api/v1/documents/upload mit Multipart-Formulardaten aufgerufen wird
    // THEN wird der HTTP-Status 201 Created und das erstellte DocumentResponseDto zurückgegeben
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

        DocumentResponseDto savedDto = DocumentResponseDto.builder()
                .id(42L)
                .title("Custom Title")
                .originalFilename("sample.pdf")
                .contentType("application/pdf")
                .fileSize(17L)
                .build();

        when(documentService.uploadDocument(any(DocumentModel.class), any(byte[].class)))
                .thenReturn(savedModel);
        when(documentDtoMapper.toResponseDto(savedModel)).thenReturn(savedDto);

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("title", "Custom Title"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.title").value("Custom Title"))
                .andExpect(jsonPath("$.originalFilename").value("sample.pdf"));
    }

    // GIVEN eine leere Datei im Upload-Request
    // WHEN POST /api/v1/documents/upload aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request zurückgegeben
    @Test
    void uploadDocument_WithEmptyFile_ReturnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:invalid-document"))
                .andExpect(jsonPath("$.detail").value("Uploaded file must not be empty"));
        verify(documentService, never()).uploadDocument(any(), any());
    }

    // GIVEN eine gültige PDF-Datei, aber RabbitMQ ist nicht erreichbar
    // WHEN POST /api/v1/documents/upload aufgerufen wird
    // THEN wird der HTTP-Status 503 Service Unavailable als ProblemDetail zurückgegeben
    @Test
    void uploadDocument_WhenQueueUnavailable_ReturnsServiceUnavailable() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.pdf", "application/pdf", "Dummy PDF content".getBytes());

        when(documentService.uploadDocument(any(DocumentModel.class), any(byte[].class)))
                .thenThrow(new MessageQueueException("Document 1 could not be queued for OCR"));

        mockMvc.perform(multipart("/api/v1/documents/upload").file(file))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Message Queue Unavailable"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:message-queue-unavailable"));
    }

    // --- Action 6: updateDocument ---

    // GIVEN ein existierendes Dokument und ein gültiges DocumentUpdateDto
    // WHEN PUT /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 200 OK und das aktualisierte Dokument zurückgegeben
    @Test
    void updateDocument_WithValidDto_ReturnsOk() throws Exception {
        DocumentUpdateDto requestDto = DocumentUpdateDto.builder().title("Renamed Invoice").build();
        DocumentModel changes = DocumentModel.builder().title("Renamed Invoice").build();
        DocumentModel updatedModel = DocumentModel.builder().id(7L).title("Renamed Invoice").build();
        DocumentResponseDto updatedDto = DocumentResponseDto.builder().id(7L).title("Renamed Invoice").build();

        when(documentDtoMapper.toModel(any(DocumentUpdateDto.class))).thenReturn(changes);
        when(documentService.update(7L, changes)).thenReturn(updatedModel);
        when(documentDtoMapper.toResponseDto(updatedModel)).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/documents/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.title").value("Renamed Invoice"));
    }

    // GIVEN ein DocumentUpdateDto mit leerem Titel
    // WHEN PUT /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 400 Bad Request wegen Validierungsfehlern zurückgegeben
    @Test
    void updateDocument_WithBlankTitle_ReturnsBadRequest() throws Exception {
        DocumentUpdateDto invalidDto = DocumentUpdateDto.builder().title(" ").build();

        mockMvc.perform(put("/api/v1/documents/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:validation-error"))
                .andExpect(jsonPath("$.invalidParams.title").exists());
        verify(documentService, never()).update(any(), any());
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN PUT /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 404 Not Found zurückgegeben
    @Test
    void updateDocument_WithNonExistingId_ReturnsNotFound() throws Exception {
        DocumentUpdateDto requestDto = DocumentUpdateDto.builder().title("Renamed").build();
        when(documentDtoMapper.toModel(any(DocumentUpdateDto.class))).thenReturn(DocumentModel.builder().title("Renamed").build());
        when(documentService.update(eq(99L), any(DocumentModel.class)))
                .thenThrow(new DocumentNotFoundException("Document with id 99 not found"));

        mockMvc.perform(put("/api/v1/documents/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:document-not-found"));
    }

    // --- Action 7: deleteDocument ---

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN DELETE /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 204 No Content zurückgegeben
    @Test
    void deleteDocument_WithValidId_ReturnsNoContent() throws Exception {
        doNothing().when(documentService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN DELETE /api/v1/documents/{id} aufgerufen wird
    // THEN wird der HTTP-Status 204 No Content idempotent zurückgegeben
    @Test
    void deleteDocument_WithNonExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(documentService).deleteById(99L);

        mockMvc.perform(delete("/api/v1/documents/99"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteById(99L);
    }

    // GIVEN ein nebenläufiger Änderungskonflikt (Optimistic Locking Failure)
    // WHEN POST /api/v1/documents aufgerufen wird und die DB einen Versionskonflikt meldet
    // THEN wird der HTTP-Status 409 Conflict und eine aussagekräftige Fehlermeldung zurückgegeben
    @Test
    void createDocument_WhenConcurrentConflictOccurs_ReturnsConflict() throws Exception {
        DocumentCreateDto requestDto = DocumentCreateDto.builder().title("Concurrent Invoice").build();
        DocumentModel model = DocumentModel.builder().title("Concurrent Invoice").build();

        when(documentDtoMapper.toModel(any(DocumentCreateDto.class))).thenReturn(model);
        when(documentService.save(any(DocumentModel.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(DocumentModel.class, 1L));

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("The document was modified concurrently by another transaction. Please refresh and try again."))
                .andExpect(jsonPath("$.type").value("urn:paperless:problem:optimistic-lock-conflict"));
    }
}
