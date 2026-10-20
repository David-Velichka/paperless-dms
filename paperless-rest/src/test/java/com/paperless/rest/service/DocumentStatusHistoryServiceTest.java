package com.paperless.rest.service;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.dal.mapper.DocumentEntityMapper;
import com.paperless.rest.mapper.DocumentStatusHistoryMapper;
import com.paperless.rest.service.impl.DocumentStatusHistoryServiceImpl;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import com.paperless.rest.service.validation.DocumentStatusHistoryModelValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStatusHistoryServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentStatusHistoryRepository documentStatusHistoryRepository;

    @Mock
    private DocumentEntityMapper documentEntityMapper;

    @Mock
    private DocumentStatusHistoryMapper documentStatusHistoryMapper;

    @Mock
    private DocumentStatusHistoryModelValidator historyModelValidator;

    @InjectMocks
    private DocumentStatusHistoryServiceImpl documentStatusHistoryService;

    private DocumentEntity sampleDocument;
    private DocumentModel sampleDocumentModel;
    private DocumentStatusHistoryEntity sampleHistoryEntity;
    private DocumentStatusHistoryModel sampleHistoryModel;

    @BeforeEach
    void setUp() {
        sampleDocument = DocumentEntity.builder()
                .id(1L)
                .title("Sample Document")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        sampleDocumentModel = DocumentModel.builder()
                .id(1L)
                .title("Sample Document")
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        sampleHistoryEntity = DocumentStatusHistoryEntity.builder()
                .id(10L)
                .documentId(1L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("worker")
                .timestamp(OffsetDateTime.now())
                .comment("OCR in progress")
                .build();

        sampleHistoryModel = DocumentStatusHistoryModel.builder()
                .id(10L)
                .documentId(1L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("worker")
                .timestamp(OffsetDateTime.now())
                .comment("OCR in progress")
                .build();
    }

    // GIVEN ein existierendes Dokument und ein gültiger Folge-Status
    // WHEN changeStatus aufgerufen wird
    // THEN wird der Status aktualisiert und ein Verlaufseintrag gespeichert
    @Test
    void changeStatus_WithValidTransition_UpdatesStatusAndLogsHistory() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));
        when(documentEntityMapper.toModel(sampleDocument)).thenReturn(sampleDocumentModel);
        doNothing().when(historyModelValidator).validateOrThrow(any(DocumentStatusHistoryModel.class));
        when(documentStatusHistoryMapper.toEntity(any(DocumentStatusHistoryModel.class))).thenReturn(sampleHistoryEntity);
        when(documentStatusHistoryRepository.save(any(DocumentStatusHistoryEntity.class))).thenReturn(sampleHistoryEntity);
        when(documentStatusHistoryMapper.toModel(sampleHistoryEntity)).thenReturn(sampleHistoryModel);

        DocumentStatusHistoryModel result = documentStatusHistoryService.changeStatus(
                1L, DocumentStatus.PROCESSING, "worker", "OCR in progress");

        assertNotNull(result);
        assertEquals(DocumentStatus.PROCESSING, result.getStatus());
        assertEquals("worker", result.getChangedBy());
        verify(historyModelValidator, times(1)).validateOrThrow(any(DocumentStatusHistoryModel.class));
        verify(documentStatusHistoryRepository, times(1)).save(any(DocumentStatusHistoryEntity.class));
    }

    // GIVEN eine Null-Dokumenten-ID bei der Statusänderung
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithNullDocumentId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.changeStatus(null, DocumentStatus.PROCESSING, "user", null));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN ein Null-Zielstatus bei der Statusänderung
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithNullTargetStatus_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.changeStatus(1L, null, "user", null));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN eine nicht existierende Dokumenten-ID
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WhenDocumentNotFound_ThrowsBusinessLayerException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.changeStatus(99L, DocumentStatus.PROCESSING, "user", null));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN ein Dokument mit Status COMPLETED
    // WHEN ein unzulässiger Übergang zu PROCESSING versucht wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithIllegalTransition_ThrowsBusinessLayerException() {
        sampleDocument.setCurrentStatus(DocumentStatus.COMPLETED);
        sampleDocumentModel = sampleDocumentModel.toBuilder().currentStatus(DocumentStatus.COMPLETED).build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));
        when(documentEntityMapper.toModel(sampleDocument)).thenReturn(sampleDocumentModel);

        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.changeStatus(1L, DocumentStatus.PROCESSING, "user", null));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN ein Statusänderungs-Modell mit Validierungsfehler
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithValidationFailure_ThrowsBusinessLayerException() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));
        when(documentEntityMapper.toModel(sampleDocument)).thenReturn(sampleDocumentModel);
        doThrow(new BusinessLayerException("Validation failed for StatusHistory: Changed by actor must not be blank"))
                .when(historyModelValidator).validateOrThrow(any(DocumentStatusHistoryModel.class));

        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.changeStatus(1L, DocumentStatus.PROCESSING, " ", null));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN eine existierende Dokumenten-ID
    // WHEN getHistoryByDocumentId aufgerufen wird
    // THEN wird die chronologische Liste der Verlaufseinträge zurückgegeben
    @Test
    void getHistoryByDocumentId_WhenExists_ReturnsHistoryList() {
        when(documentRepository.existsById(1L)).thenReturn(true);
        when(documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(sampleHistoryEntity));
        when(documentStatusHistoryMapper.toModelList(List.of(sampleHistoryEntity)))
                .thenReturn(List.of(sampleHistoryModel));

        List<DocumentStatusHistoryModel> result = documentStatusHistoryService.getHistoryByDocumentId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    // GIVEN eine nicht existierende Dokumenten-ID beim Abrufen des Verlaufs
    // WHEN getHistoryByDocumentId aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void getHistoryByDocumentId_WhenNotFound_ThrowsBusinessLayerException() {
        when(documentRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.getHistoryByDocumentId(99L));
    }

    // GIVEN eine Null-Dokumenten-ID beim Abrufen des Verlaufs
    // WHEN getHistoryByDocumentId aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void getHistoryByDocumentId_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.getHistoryByDocumentId(null));
    }

    // GIVEN eine Dokument-ID für den Initial-Verlauf
    // WHEN createInitialHistory aufgerufen wird
    // THEN wird ein Eintrag mit Status RECEIVED angelegt
    @Test
    void createInitialHistory_WithValidData_CreatesReceivedEntry() {
        DocumentStatusHistoryEntity initialEntity = DocumentStatusHistoryEntity.builder()
                .id(1L)
                .documentId(5L)
                .status(DocumentStatus.RECEIVED)
                .changedBy("uploader")
                .comment("Initial upload")
                .build();

        DocumentStatusHistoryModel initialModel = DocumentStatusHistoryModel.builder()
                .id(1L)
                .documentId(5L)
                .status(DocumentStatus.RECEIVED)
                .changedBy("uploader")
                .comment("Initial upload")
                .build();

        doNothing().when(historyModelValidator).validateOrThrow(any(DocumentStatusHistoryModel.class));
        when(documentStatusHistoryMapper.toEntity(any(DocumentStatusHistoryModel.class))).thenReturn(initialEntity);
        when(documentStatusHistoryRepository.save(any(DocumentStatusHistoryEntity.class))).thenReturn(initialEntity);
        when(documentStatusHistoryMapper.toModel(initialEntity)).thenReturn(initialModel);

        DocumentStatusHistoryModel result = documentStatusHistoryService.createInitialHistory(5L, "uploader", "Initial upload");

        assertNotNull(result);
        assertEquals(DocumentStatus.RECEIVED, result.getStatus());
        verify(historyModelValidator, times(1)).validateOrThrow(any(DocumentStatusHistoryModel.class));
        verify(documentStatusHistoryRepository, times(1)).save(any(DocumentStatusHistoryEntity.class));
    }

    // GIVEN eine Null-Dokument-ID beim Erstellen des Initial-Verlaufs
    // WHEN createInitialHistory aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void createInitialHistory_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.createInitialHistory(null, "uploader", "test"));
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN eine existierende Verlaufs-ID
    // WHEN findById aufgerufen wird
    // THEN wird das Modell zurückgegeben
    @Test
    void findById_WhenExists_ReturnsModel() {
        when(documentStatusHistoryRepository.findById(10L)).thenReturn(Optional.of(sampleHistoryEntity));
        when(documentStatusHistoryMapper.toModel(sampleHistoryEntity)).thenReturn(sampleHistoryModel);

        DocumentStatusHistoryModel result = documentStatusHistoryService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    // GIVEN eine nicht existierende Verlaufs-ID
    // WHEN findById aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WhenNotFound_ThrowsBusinessLayerException() {
        when(documentStatusHistoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.findById(99L));
    }

    // GIVEN eine existierende Dokumenten-ID zum Löschen aller Verläufe
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN wird die Löschmethode des Repositories ausgeführt
    @Test
    void deleteByDocumentId_WithValidId_ExecutesDeletion() {
        doNothing().when(documentStatusHistoryRepository).deleteByDocumentId(1L);

        assertDoesNotThrow(() -> documentStatusHistoryService.deleteByDocumentId(1L));

        verify(documentStatusHistoryRepository, times(1)).deleteByDocumentId(1L);
    }

    // GIVEN eine Null-Dokumenten-ID zum Löschen aller Verläufe
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void deleteByDocumentId_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class,
                () -> documentStatusHistoryService.deleteByDocumentId(null));
        verify(documentStatusHistoryRepository, never()).deleteByDocumentId(any());
    }
}
