package com.paperless.rest.service;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.DocumentNotFoundException;
import com.paperless.rest.exception.MessageQueueException;
import com.paperless.rest.exception.StorageException;
import com.paperless.rest.service.impl.DocumentServiceImpl;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.validation.DocumentModelValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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

    @Mock
    private OcrJobPublisher ocrJobPublisher;

    @Mock
    private DocumentStorage documentStorage;

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
                .createInitialHistory(5L, "Document metadata created");
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
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));
        doNothing().when(documentStatusHistoryService).deleteByDocumentId(1L);
        doNothing().when(documentPersistenceService).deleteById(1L);

        assertDoesNotThrow(() -> documentService.deleteById(1L));

        verify(documentStatusHistoryService, times(1)).deleteByDocumentId(1L);
        verify(documentPersistenceService, times(1)).deleteById(1L);
    }

    // GIVEN ein existierendes Dokument mit Storage-Key
    // WHEN deleteById aufgerufen wird
    // THEN wird auch die Datei im Object Storage gelöscht
    @Test
    void deleteById_WhenDocumentHasStoragePath_DeletesFileFromStorage() {
        DocumentModel withFile = sampleModel.toBuilder().storagePath("abc.pdf").build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(withFile));

        documentService.deleteById(1L);

        verify(documentStorage, times(1)).delete("abc.pdf");
    }

    // GIVEN ein existierendes Dokument ohne Storage-Key
    // WHEN deleteById aufgerufen wird
    // THEN wird der Object Storage nicht aufgerufen
    @Test
    void deleteById_WhenDocumentHasNoStoragePath_SkipsStorage() {
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));

        documentService.deleteById(1L);

        verify(documentPersistenceService, times(1)).deleteById(1L);
        verifyNoInteractions(documentStorage);
    }

    // GIVEN ein existierendes Dokument, aber der Object Storage ist nicht erreichbar
    // WHEN deleteById aufgerufen wird
    // THEN wird die StorageException weitergegeben, damit die Transaktion zurückgerollt wird
    @Test
    void deleteById_WhenStorageFails_PropagatesStorageException() {
        DocumentModel withFile = sampleModel.toBuilder().storagePath("abc.pdf").build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(withFile));
        doThrow(new StorageException("Document file could not be deleted", new RuntimeException("down")))
                .when(documentStorage).delete("abc.pdf");

        assertThrows(StorageException.class, () -> documentService.deleteById(1L));
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN wird die Operation idempotent ohne Ausnahme abgeschlossen
    @Test
    void deleteById_WhenNotFound_CompletesIdempotentlyWithoutException() {
        when(documentPersistenceService.findById(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> documentService.deleteById(99L));
        verify(documentPersistenceService, never()).deleteById(any());
        verify(documentStatusHistoryService, never()).deleteByDocumentId(any());
        verifyNoInteractions(documentStorage);
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
                .createInitialHistory(10L, "Document file uploaded");
        verify(ocrJobPublisher, times(1)).sendOcrMessage(expectedModel);
    }

    // GIVEN ein gültiges DocumentModel und Dateiinhalt
    // WHEN uploadDocument aufgerufen wird
    // THEN wird die Datei unter einem UUID-Key gespeichert und derselbe Key als storagePath abgelegt
    @Test
    void uploadDocument_StoresFileAndSetsStoragePath() {
        DocumentModel inputModel = DocumentModel.builder()
                .title("Upload Test").originalFilename("test.pdf").contentType("application/pdf").build();
        when(documentPersistenceService.save(any(DocumentModel.class)))
                .thenAnswer(invocation -> ((DocumentModel) invocation.getArgument(0)).toBuilder().id(10L).build());
        byte[] content = "dummy content".getBytes();

        DocumentModel result = documentService.uploadDocument(inputModel, content);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(documentStorage).store(keyCaptor.capture(), eq(content), eq("application/pdf"));
        String key = keyCaptor.getValue();
        assertTrue(key.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.pdf"));
        assertEquals(key, result.getStoragePath());
        verify(documentStorage, never()).delete(any());
    }

    // GIVEN ein gültiges Dokument, aber der Object Storage ist nicht erreichbar
    // WHEN uploadDocument aufgerufen wird
    // THEN wird die StorageException weitergegeben und nichts in Datenbank oder Queue geschrieben
    @Test
    void uploadDocument_WhenStorageFails_SavesNothing() {
        DocumentModel inputModel = DocumentModel.builder().title("Upload Test").originalFilename("test.pdf").build();
        doThrow(new StorageException("Document file could not be stored", new RuntimeException("down")))
                .when(documentStorage).store(anyString(), any(byte[].class), any());

        assertThrows(StorageException.class,
                () -> documentService.uploadDocument(inputModel, "content".getBytes()));

        verify(documentPersistenceService, never()).save(any());
        verify(documentStatusHistoryService, never()).createInitialHistory(any(), any());
        verify(ocrJobPublisher, never()).sendOcrMessage(any());
        verify(documentStorage, never()).delete(any());
    }

    // GIVEN ein gespeicherter Blob, aber das Speichern der Metadaten schlägt fehl
    // WHEN uploadDocument aufgerufen wird
    // THEN wird der Blob wieder gelöscht und die ursprüngliche Exception weitergegeben
    @Test
    void uploadDocument_WhenSaveFails_DeletesStoredFile() {
        DocumentModel inputModel = DocumentModel.builder().title("Upload Test").originalFilename("test.pdf").build();
        when(documentPersistenceService.save(any(DocumentModel.class))).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class,
                () -> documentService.uploadDocument(inputModel, "content".getBytes()));

        ArgumentCaptor<String> storedKey = ArgumentCaptor.forClass(String.class);
        verify(documentStorage).store(storedKey.capture(), any(byte[].class), any());
        verify(documentStorage).delete(storedKey.getValue());
    }

    // GIVEN ein gespeicherter Blob, aber das Löschen beim Kompensieren schlägt ebenfalls fehl
    // WHEN uploadDocument aufgerufen wird
    // THEN wird trotzdem die ursprüngliche Exception weitergegeben
    @Test
    void uploadDocument_WhenCompensationFails_StillThrowsOriginalException() {
        DocumentModel inputModel = DocumentModel.builder().title("Upload Test").originalFilename("test.pdf").build();
        when(documentPersistenceService.save(any(DocumentModel.class))).thenThrow(new IllegalStateException("db down"));
        doThrow(new StorageException("Document file could not be deleted", new RuntimeException("down")))
                .when(documentStorage).delete(anyString());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> documentService.uploadDocument(inputModel, "content".getBytes()));

        assertEquals("db down", ex.getMessage());
    }

    // GIVEN ein gültiges Dokument, aber RabbitMQ ist nicht erreichbar
    // WHEN uploadDocument aufgerufen wird
    // THEN wird die MessageQueueException weitergegeben, damit die Transaktion zurückgerollt wird
    @Test
    void uploadDocument_WhenQueueUnavailable_ThrowsMessageQueueException() {
        DocumentModel inputModel = DocumentModel.builder().title("Upload Test").originalFilename("test.pdf").build();
        DocumentModel savedModel = inputModel.toBuilder().id(11L).currentStatus(DocumentStatus.RECEIVED).build();

        when(documentPersistenceService.save(any(DocumentModel.class))).thenReturn(savedModel);
        doThrow(new MessageQueueException("Document 11 could not be queued for OCR"))
                .when(ocrJobPublisher).sendOcrMessage(savedModel);

        assertThrows(MessageQueueException.class,
                () -> documentService.uploadDocument(inputModel, "content".getBytes()));
        // Compensation: the file stored before is removed again
        ArgumentCaptor<String> storedKey = ArgumentCaptor.forClass(String.class);
        verify(documentStorage).store(storedKey.capture(), any(byte[].class), any());
        verify(documentStorage).delete(storedKey.getValue());
    }

    // GIVEN ein Null-DocumentModel beim Upload
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(null, "content".getBytes()));
        verify(documentPersistenceService, never()).save(any());
        verify(ocrJobPublisher, never()).sendOcrMessage(any());
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

    // GIVEN ein existierendes Dokument und ein neuer Titel
    // WHEN update aufgerufen wird
    // THEN wird nur der Titel geändert und das Dokument gespeichert
    @Test
    void update_WhenExists_ChangesTitleAndSaves() {
        DocumentModel changes = DocumentModel.builder().title("Renamed Invoice").build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));
        when(documentPersistenceService.save(any(DocumentModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentModel result = documentService.update(1L, changes);

        assertEquals(1L, result.getId());
        assertEquals("Renamed Invoice", result.getTitle());
        assertEquals(DocumentStatus.RECEIVED, result.getCurrentStatus());
        verify(documentStatusHistoryService, never()).createInitialHistory(any(), any());
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN update aufgerufen wird
    // THEN wird eine DocumentNotFoundException geworfen
    @Test
    void update_WhenNotFound_ThrowsDocumentNotFoundException() {
        DocumentModel changes = DocumentModel.builder().title("Renamed").build();
        when(documentPersistenceService.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> documentService.update(99L, changes));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN keine Änderungen (null)
    // WHEN update aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void update_WithNullChanges_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.update(1L, null));
        verify(documentPersistenceService, never()).save(any());
    }

    // GIVEN ein Dokument im Status RECEIVED
    // WHEN markOcrStarted aufgerufen wird
    // THEN wechselt der Status mit Kommentar auf PROCESSING
    @Test
    void markOcrStarted_FromReceived_ChangesToProcessing() {
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));

        documentService.markOcrStarted(1L);

        verify(documentStatusHistoryService).changeStatus(1L, DocumentStatus.PROCESSING, "OCR started");
    }

    // GIVEN ein Dokument, das bereits im Status PROCESSING ist (wiederholtes STARTED-Event)
    // WHEN markOcrStarted aufgerufen wird
    // THEN wird das Event ignoriert und der Status nicht geändert
    @Test
    void markOcrStarted_WhenAlreadyProcessing_IgnoresDuplicate() {
        DocumentModel processing = sampleModel.toBuilder().currentStatus(DocumentStatus.PROCESSING).build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(processing));

        assertDoesNotThrow(() -> documentService.markOcrStarted(1L));

        verifyNoInteractions(documentStatusHistoryService);
    }

    // GIVEN ein Dokument, das inzwischen gelöscht wurde
    // WHEN markOcrStarted aufgerufen wird
    // THEN wird das Event ohne Exception ignoriert
    @Test
    void markOcrStarted_WhenDocumentMissing_IgnoresEvent() {
        when(documentPersistenceService.findById(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> documentService.markOcrStarted(99L));

        verifyNoInteractions(documentStatusHistoryService);
    }

    // GIVEN ein Dokument im Status PROCESSING
    // WHEN completeOcr mit dem erkannten Text aufgerufen wird
    // THEN wird zuerst der Text gespeichert und danach der Status auf REVIEW_PENDING gesetzt
    @Test
    void completeOcr_FromProcessing_StoresTextThenChangesToReviewPending() {
        DocumentModel processing = sampleModel.toBuilder().currentStatus(DocumentStatus.PROCESSING).build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(processing));

        documentService.completeOcr(1L, "Hello World");

        ArgumentCaptor<DocumentModel> captor = ArgumentCaptor.forClass(DocumentModel.class);
        InOrder order = inOrder(documentPersistenceService, documentStatusHistoryService);
        order.verify(documentPersistenceService).save(captor.capture());
        order.verify(documentStatusHistoryService).changeStatus(1L, DocumentStatus.REVIEW_PENDING, "OCR completed");
        assertEquals("Hello World", captor.getValue().getOcrText());
        verify(documentStatusHistoryService, never()).changeStatus(1L, DocumentStatus.PROCESSING, "OCR started");
    }

    // GIVEN ein Dokument im Status RECEIVED (STARTED-Event fehlt oder kommt später)
    // WHEN completeOcr aufgerufen wird
    // THEN werden beide Schritte PROCESSING und REVIEW_PENDING nacheinander durchgeführt
    @Test
    void completeOcr_FromReceived_WalksBothSteps() {
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(sampleModel));

        documentService.completeOcr(1L, "Hello World");

        InOrder order = inOrder(documentStatusHistoryService);
        order.verify(documentStatusHistoryService).changeStatus(1L, DocumentStatus.PROCESSING, "OCR started");
        order.verify(documentStatusHistoryService).changeStatus(1L, DocumentStatus.REVIEW_PENDING, "OCR completed");
    }

    // GIVEN ein bereits abgelehntes Dokument (REJECTED)
    // WHEN completeOcr aufgerufen wird
    // THEN wird der Text gespeichert und der Status bleibt unverändert
    @Test
    void completeOcr_ForRejectedDocument_StoresTextAndKeepsStatus() {
        DocumentModel rejected = sampleModel.toBuilder().currentStatus(DocumentStatus.REJECTED).build();
        when(documentPersistenceService.findById(1L)).thenReturn(Optional.of(rejected));

        assertDoesNotThrow(() -> documentService.completeOcr(1L, "Hello World"));

        ArgumentCaptor<DocumentModel> captor = ArgumentCaptor.forClass(DocumentModel.class);
        verify(documentPersistenceService).save(captor.capture());
        assertEquals("Hello World", captor.getValue().getOcrText());
        assertEquals(DocumentStatus.REJECTED, captor.getValue().getCurrentStatus());
        verifyNoInteractions(documentStatusHistoryService);
    }

    // GIVEN ein Dokument, das inzwischen gelöscht wurde
    // WHEN completeOcr aufgerufen wird
    // THEN wird das Ergebnis ohne Exception ignoriert und nichts gespeichert
    @Test
    void completeOcr_WhenDocumentMissing_IgnoresResult() {
        when(documentPersistenceService.findById(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> documentService.completeOcr(99L, "Hello World"));

        verify(documentPersistenceService, never()).save(any());
        verifyNoInteractions(documentStatusHistoryService);
    }
}
