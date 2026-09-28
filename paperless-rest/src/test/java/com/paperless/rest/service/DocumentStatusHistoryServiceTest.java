package com.paperless.rest.service;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.mapper.DocumentStatusHistoryMapper;
import com.paperless.rest.service.impl.DocumentStatusHistoryServiceImpl;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
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
    private DocumentMapper documentMapper;

    @Mock
    private DocumentStatusHistoryMapper documentStatusHistoryMapper;

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
        when(documentMapper.toModel(sampleDocument)).thenReturn(sampleDocumentModel);
        when(documentStatusHistoryRepository.save(any(DocumentStatusHistoryEntity.class))).thenReturn(sampleHistoryEntity);
        when(documentStatusHistoryMapper.toModel(sampleHistoryEntity)).thenReturn(sampleHistoryModel);

        DocumentStatusHistoryModel result = documentStatusHistoryService.changeStatus(
                1L, DocumentStatus.PROCESSING, "worker", "OCR in progress");

        assertNotNull(result);
        assertEquals(DocumentStatus.PROCESSING, result.getStatus());
        assertEquals(DocumentStatus.RECEIVED, result.getPreviousStatus());
        verify(documentMapper, times(1)).updateEntityFromModel(sampleDocumentModel, sampleDocument);
        verify(documentRepository, times(1)).save(sampleDocument);
        verify(documentStatusHistoryRepository, times(1)).save(any(DocumentStatusHistoryEntity.class));
    }

    // GIVEN ein existierendes Dokument im Status RECEIVED
    // WHEN ein unzulässiger Übergang zu COMPLETED angefordert wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithIllegalTransition_ThrowsBusinessLayerException() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));
        when(documentMapper.toModel(sampleDocument)).thenReturn(sampleDocumentModel);

        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.changeStatus(1L, DocumentStatus.COMPLETED, "user", "Jump directly"));

        verify(documentRepository, never()).save(any());
        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN eine nicht existierende Dokumenten-ID
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithNonExistingDocument_ThrowsBusinessLayerException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.changeStatus(999L, DocumentStatus.PROCESSING, "worker", null));

        verify(documentStatusHistoryRepository, never()).save(any());
    }

    // GIVEN eine Null-Dokumenten-ID
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithNullDocumentId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.changeStatus(null, DocumentStatus.PROCESSING, "worker", null));
    }

    // GIVEN ein Null-Zielstatus
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithNullTargetStatus_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.changeStatus(1L, null, "worker", null));
    }

    // GIVEN ein leerer Akteur
    // WHEN changeStatus aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void changeStatus_WithBlankChangedBy_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.changeStatus(1L, DocumentStatus.PROCESSING, "   ", null));
    }

    // GIVEN ein existierendes Dokument mit Verlaufseinträgen
    // WHEN getHistoryByDocumentId aufgerufen wird
    // THEN wird die chronologische Liste der Statuseinträge zurückgegeben
    @Test
    void getHistoryByDocumentId_WhenDocumentExists_ReturnsHistoryList() {
        when(documentRepository.existsById(1L)).thenReturn(true);
        when(documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(sampleHistoryEntity));
        when(documentStatusHistoryMapper.toModelList(List.of(sampleHistoryEntity)))
                .thenReturn(List.of(sampleHistoryModel));

        List<DocumentStatusHistoryModel> history = documentStatusHistoryService.getHistoryByDocumentId(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(DocumentStatus.PROCESSING, history.get(0).getStatus());
    }

    // GIVEN eine nicht existierende Dokumenten-ID
    // WHEN getHistoryByDocumentId aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void getHistoryByDocumentId_WhenDocumentNotFound_ThrowsBusinessLayerException() {
        when(documentRepository.existsById(999L)).thenReturn(false);

        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.getHistoryByDocumentId(999L));
    }

    // GIVEN eine Dokumenten-ID für ein neu erstelltes Dokument
    // WHEN createInitialHistory aufgerufen wird
    // THEN wird ein Initialverlaufseintrag im Status RECEIVED erzeugt
    @Test
    void createInitialHistory_CreatesReceivedEntry() {
        when(documentStatusHistoryRepository.save(any(DocumentStatusHistoryEntity.class)))
                .thenReturn(sampleHistoryEntity);
        when(documentStatusHistoryMapper.toModel(sampleHistoryEntity))
                .thenReturn(sampleHistoryModel);

        DocumentStatusHistoryModel result = documentStatusHistoryService.createInitialHistory(
                1L, "uploader", "Initial upload");

        assertNotNull(result);
        verify(documentStatusHistoryRepository, times(1)).save(any(DocumentStatusHistoryEntity.class));
    }

    // GIVEN eine existierende Verlaufs-ID
    // WHEN findById aufgerufen wird
    // THEN wird der gemappte Verlaufseintrag zurückgegeben
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

        assertThrows(BusinessLayerException.class, () ->
                documentStatusHistoryService.findById(99L));
    }

    // GIVEN eine gültige Dokumenten-ID
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN werden die Verlaufsdaten gelöscht
    @Test
    void deleteByDocumentId_CallsRepository() {
        documentStatusHistoryService.deleteByDocumentId(1L);

        verify(documentStatusHistoryRepository, times(1)).deleteByDocumentId(1L);
    }
}
