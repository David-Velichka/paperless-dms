package com.paperless.rest.dal.persistence;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.mapper.DocumentStatusHistoryEntityMapper;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStatusHistoryPersistenceServiceTest {

    @Mock
    private DocumentStatusHistoryRepository documentStatusHistoryRepository;

    @Mock
    private DocumentStatusHistoryEntityMapper documentStatusHistoryEntityMapper;

    @InjectMocks
    private DocumentStatusHistoryPersistenceService persistenceService;

    private DocumentStatusHistoryModel sampleModel;
    private DocumentStatusHistoryEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleModel = DocumentStatusHistoryModel.builder()
                .id(1L)
                .documentId(10L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .timestamp(OffsetDateTime.now())
                .comment("OCR started")
                .build();

        sampleEntity = DocumentStatusHistoryEntity.builder()
                .id(1L)
                .documentId(10L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .timestamp(OffsetDateTime.now())
                .comment("OCR started")
                .build();
    }

    // GIVEN ein gültiges DocumentStatusHistoryModel
    // WHEN save aufgerufen wird
    // THEN wird das gemappte Entity gespeichert und als Model zurückgegeben
    @Test
    void save_MapsAndPersistsHistoryModel() {
        when(documentStatusHistoryEntityMapper.toEntity(sampleModel)).thenReturn(sampleEntity);
        when(documentStatusHistoryRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(documentStatusHistoryEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        DocumentStatusHistoryModel result = persistenceService.save(sampleModel);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(DocumentStatus.PROCESSING, result.getStatus());
        verify(documentStatusHistoryEntityMapper, times(1)).toEntity(sampleModel);
        verify(documentStatusHistoryRepository, times(1)).save(sampleEntity);
        verify(documentStatusHistoryEntityMapper, times(1)).toModel(sampleEntity);
    }

    // GIVEN eine Dokument-ID mit Verlaufseinträgen
    // WHEN findByDocumentIdOrderByTimestampDesc aufgerufen wird
    // THEN wird die chronologische Liste gemappter Modelle zurückgegeben
    @Test
    void findByDocumentIdOrderByTimestampDesc_ReturnsMappedList() {
        when(documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(sampleEntity));
        when(documentStatusHistoryEntityMapper.toModelList(List.of(sampleEntity)))
                .thenReturn(List.of(sampleModel));

        List<DocumentStatusHistoryModel> result = persistenceService.findByDocumentIdOrderByTimestampDesc(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getDocumentId());
        verify(documentStatusHistoryRepository, times(1)).findByDocumentIdOrderByTimestampDesc(10L);
    }

    // GIVEN eine existierende Verlaufs-ID
    // WHEN findById aufgerufen wird
    // THEN wird ein Optional mit dem gemappten Model zurückgegeben
    @Test
    void findById_WhenExists_ReturnsMappedModel() {
        when(documentStatusHistoryRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(documentStatusHistoryEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        Optional<DocumentStatusHistoryModel> result = persistenceService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(documentStatusHistoryRepository, times(1)).findById(1L);
    }

    // GIVEN eine nicht existierende Verlaufs-ID
    // WHEN findById aufgerufen wird
    // THEN wird ein leeres Optional zurückgegeben
    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        when(documentStatusHistoryRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DocumentStatusHistoryModel> result = persistenceService.findById(99L);

        assertTrue(result.isEmpty());
        verify(documentStatusHistoryRepository, times(1)).findById(99L);
    }

    // GIVEN eine gültige Dokument-ID zum Löschen aller Verläufe
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN wird die Repository-Löschmethode aufgerufen
    @Test
    void deleteByDocumentId_WhenValidId_DeletesFromRepository() {
        doNothing().when(documentStatusHistoryRepository).deleteByDocumentId(10L);

        persistenceService.deleteByDocumentId(10L);

        verify(documentStatusHistoryRepository, times(1)).deleteByDocumentId(10L);
    }

    // GIVEN eine Null-Dokument-ID zum Löschen aller Verläufe
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN wird keine Repository-Löschmethode aufgerufen
    @Test
    void deleteByDocumentId_WhenNullId_DoesNothing() {
        persistenceService.deleteByDocumentId(null);

        verify(documentStatusHistoryRepository, never()).deleteByDocumentId(any());
    }
}
