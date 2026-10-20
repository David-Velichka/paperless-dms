package com.paperless.rest.service;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.impl.DocumentServiceImpl;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.validation.DocumentModelValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentPersistenceService documentPersistenceService;

    @Mock
    private DocumentStatusHistoryService documentStatusHistoryService;

    @Mock
    private DocumentModelValidator documentModelValidator;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private DocumentModel sampleModel;

    @BeforeEach
    void setUp() {
        sampleModel = DocumentModel.builder()
                .id(1L)
                .title("Test Invoice")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();
    }

    // GIVEN ein gültiges DocumentModel
    // WHEN save im Service aufgerufen wird
    // THEN wird das Modell über den Persistence Service gespeichert und zurückgegeben
    @Test
    void save_WithValidModel_ReturnsSavedModel() {
        doNothing().when(documentModelValidator).validateOrThrow(sampleModel);
        when(documentPersistenceService.save(sampleModel)).thenReturn(sampleModel);

        DocumentModel result = documentService.save(sampleModel);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Invoice", result.getTitle());
        verify(documentModelValidator, times(1)).validateOrThrow(sampleModel);
        verify(documentPersistenceService, times(1)).save(sampleModel);
    }

    // GIVEN ein neues DocumentModel ohne Status
    // WHEN save aufgerufen wird
    // THEN wird der Status RECEIVED initialisiert und Initial-Verlauf erstellt
    @Test
    void save_NewDocumentWithoutStatus_InitializesReceivedAndCreatesHistory() {
        DocumentModel newModel = DocumentModel.builder()
                .title("New Invoice")
                .build();

        DocumentModel savedModel = DocumentModel.builder()
                .id(5L)
                .title("New Invoice")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        doNothing().when(documentModelValidator).validateOrThrow(newModel);
        when(documentPersistenceService.save(any(DocumentModel.class))).thenReturn(savedModel);

        DocumentModel result = documentService.save(newModel);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(DocumentStatus.RECEIVED, result.getCurrentStatus());
        verify(documentStatusHistoryService, times(1))
                .createInitialHistory(5L, "system", "Document metadata created");
    }

    // GIVEN ein Null-DocumentModel
    // WHEN save im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void save_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.save(null));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN ein DocumentModel ohne Titel
    // WHEN save im Service aufgerufen wird
    // THEN wird eine BusinessLayerException durch die Validierung geworfen
    @Test
    void save_WithBlankTitle_ThrowsBusinessLayerException() {
        DocumentModel invalid = DocumentModel.builder().title("   ").build();
        doThrow(new BusinessLayerException("Validation failed for Document: Document title must not be blank"))
                .when(documentModelValidator).validateOrThrow(invalid);

        assertThrows(BusinessLayerException.class, () -> documentService.save(invalid));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN findById im Service aufgerufen wird
    // THEN wird das gemappte DocumentModel zurückgegeben
    @Test
    void findById_WhenExists_ReturnsDocumentModel() {
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));

        DocumentModel result = documentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Invoice", result.getTitle());
        verify(documentPersistenceService, times(1)).findById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN findById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WhenNotFound_ThrowsBusinessLayerException() {
        when(documentPersistenceService.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class, () -> documentService.findById(99L));
        verify(documentPersistenceService, times(1)).findById(99L);
    }

    // GIVEN eine Null-Dokument-ID
    // WHEN findById aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.findById(null));
        verify(documentPersistenceService, never()).findById(any());
    }

    // GIVEN existierende Dokumente in der Datenbank
    // WHEN findAll aufgerufen wird
    // THEN wird eine Liste von DocumentModels zurückgegeben
    @Test
    void findAll_ReturnsAllDocuments() {
        when(documentPersistenceService.findAll()).thenReturn(List.of(sampleModel));

        List<DocumentModel> result = documentService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Invoice", result.get(0).getTitle());
    }

    // GIVEN ein Suchbegriff
    // WHEN search aufgerufen wird
    // THEN werden passende Dokumente über den Persistence Service gefiltert
    @Test
    void search_WithMatchingQuery_ReturnsFilteredDocuments() {
        when(documentPersistenceService.searchByTitle("Invoice")).thenReturn(List.of(sampleModel));

        List<DocumentModel> result = documentService.search("Invoice");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Invoice", result.get(0).getTitle());
        verify(documentPersistenceService, times(1)).searchByTitle("Invoice");
    }

    // GIVEN ein leerer Suchbegriff
    // WHEN search aufgerufen wird
    // THEN werden alle Dokumente zurückgegeben
    @Test
    void search_WithBlankQuery_DelegatesToFindAll() {
        when(documentPersistenceService.findAll()).thenReturn(List.of(sampleModel));

        List<DocumentModel> result = documentService.search("  ");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(documentPersistenceService, times(1)).findAll();
    }

    // GIVEN eine existierende Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN werden Verläufe und das Dokument gelöscht
    @Test
    void deleteById_WhenExists_DeletesDocumentAndHistory() {
        when(documentPersistenceService.existsById(1L)).thenReturn(true);
        doNothing().when(documentStatusHistoryService).deleteByDocumentId(1L);
        doNothing().when(documentPersistenceService).deleteById(1L);

        assertDoesNotThrow(() -> documentService.deleteById(1L));

        verify(documentStatusHistoryService, times(1)).deleteByDocumentId(1L);
        verify(documentPersistenceService, times(1)).deleteById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN wird die Operation idempotent ohne Ausnahme abgeschlossen
    @Test
    void deleteById_WhenNotFound_CompletesIdempotentlyWithoutException() {
        when(documentPersistenceService.existsById(99L)).thenReturn(false);

        assertDoesNotThrow(() -> documentService.deleteById(99L));
        verify(documentPersistenceService, never()).deleteById(any());
        verify(documentStatusHistoryService, never()).deleteByDocumentId(any());
    }

    // GIVEN eine Null-Dokument-ID zum Löschen
    // WHEN deleteById aufgerufen wird
    // THEN wird die Operation idempotent ohne Ausnahme abgeschlossen
    @Test
    void deleteById_WithNullId_CompletesIdempotentlyWithoutException() {
        assertDoesNotThrow(() -> documentService.deleteById(null));
        verify(documentPersistenceService, never()).deleteById(any());
        verify(documentStatusHistoryService, never()).deleteByDocumentId(any());
    }

    // GIVEN ein gültiges DocumentModel und Dateiinhalt
    // WHEN uploadDocument aufgerufen wird
    // THEN wird das Dokument validiert, initialisiert und gespeichert
    @Test
    void uploadDocument_WithValidData_SavesAndCreatesInitialHistory() {
        DocumentModel inputModel = DocumentModel.builder()
                .title("Upload Test")
                .originalFilename("test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .build();

        DocumentModel expectedModel = DocumentModel.builder()
                .id(10L)
                .title("Upload Test")
                .originalFilename("test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        doNothing().when(documentModelValidator).validateOrThrow(inputModel);
        when(documentPersistenceService.save(any(DocumentModel.class))).thenReturn(expectedModel);

        DocumentModel result = documentService.uploadDocument(inputModel, "dummy content".getBytes());

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Upload Test", result.getTitle());
        verify(documentModelValidator, times(1)).validateOrThrow(inputModel);
        verify(documentPersistenceService, times(1)).save(any(DocumentModel.class));
        verify(documentStatusHistoryService, times(1))
                .createInitialHistory(10L, "uploader", "Document file uploaded");
    }

    // GIVEN ein Null-DocumentModel beim Upload
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(null, "content".getBytes()));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN ein DocumentModel mit leerem Dateiinhalt
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithEmptyContent_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder().title("Test").build();

        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(model, new byte[0]));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN ein DocumentModel mit Validierungsfehler beim Upload
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithValidationFailure_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder().title(" ").build();
        doThrow(new BusinessLayerException("Validation failed for Document: Document title must not be blank"))
                .when(documentModelValidator).validateOrThrow(model);

        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(model, "content".getBytes()));
        verify(documentPersistenceService, never()).save(any());
    }
}
