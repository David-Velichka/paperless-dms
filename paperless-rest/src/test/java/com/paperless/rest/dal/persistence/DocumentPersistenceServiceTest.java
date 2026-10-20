package com.paperless.rest.dal.persistence;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.mapper.DocumentEntityMapper;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.service.model.DocumentModel;
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
class DocumentPersistenceServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentEntityMapper documentEntityMapper;

    @InjectMocks
    private DocumentPersistenceService persistenceService;

    private DocumentModel sampleModel;
    private DocumentEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleModel = DocumentModel.builder()
                .id(1L)
                .title("Persistence Test Doc")
                .build();

        sampleEntity = DocumentEntity.builder()
                .id(1L)
                .title("Persistence Test Doc")
                .build();
    }

    // GIVEN ein gültiges DocumentModel
    // WHEN save aufgerufen wird
    // THEN wird das gemappte Entity gespeichert und als Model zurückgegeben
    @Test
    void save_MapsAndPersistsModel() {
        when(documentEntityMapper.toEntity(sampleModel)).thenReturn(sampleEntity);
        when(documentRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(documentEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        DocumentModel result = persistenceService.save(sampleModel);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Persistence Test Doc", result.getTitle());
        verify(documentEntityMapper, times(1)).toEntity(sampleModel);
        verify(documentRepository, times(1)).save(sampleEntity);
        verify(documentEntityMapper, times(1)).toModel(sampleEntity);
    }

    // GIVEN eine existierende Dokument-ID
    // WHEN findById aufgerufen wird
    // THEN wird ein Optional mit dem gemappten DocumentModel zurückgegeben
    @Test
    void findById_WhenExists_ReturnsMappedModel() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(documentEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        Optional<DocumentModel> result = persistenceService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(documentRepository, times(1)).findById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN findById aufgerufen wird
    // THEN wird ein leeres Optional zurückgegeben
    @Test
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DocumentModel> result = persistenceService.findById(99L);

        assertTrue(result.isEmpty());
        verify(documentRepository, times(1)).findById(99L);
    }

    // GIVEN gespeicherte Dokumente in der Datenbank
    // WHEN findAll aufgerufen wird
    // THEN wird die Liste aller gemappten DocumentModels zurückgegeben
    @Test
    void findAll_ReturnsMappedModelList() {
        when(documentRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(documentEntityMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<DocumentModel> result = persistenceService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Persistence Test Doc", result.get(0).getTitle());
    }

    // GIVEN ein Suchbegriff
    // WHEN searchByTitle aufgerufen wird
    // THEN wird die Liste passender DocumentModels zurückgegeben
    @Test
    void searchByTitle_ReturnsFilteredModelList() {
        when(documentRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(sampleEntity));
        when(documentEntityMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<DocumentModel> result = persistenceService.searchByTitle("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Persistence Test Doc", result.get(0).getTitle());
        verify(documentRepository, times(1)).findByTitleContainingIgnoreCase("Test");
    }

    // GIVEN eine existierende Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN wird das Dokument im Repository gelöscht
    @Test
    void deleteById_WhenExists_DeletesEntity() {
        when(documentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(documentRepository).deleteById(1L);

        persistenceService.deleteById(1L);

        verify(documentRepository, times(1)).deleteById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN wird kein Löschvorgang im Repository aufgerufen
    @Test
    void deleteById_WhenNotExists_DoesNothing() {
        when(documentRepository.existsById(99L)).thenReturn(false);

        persistenceService.deleteById(99L);

        verify(documentRepository, never()).deleteById(any());
    }

    // GIVEN eine Null-Dokument-ID
    // WHEN deleteById aufgerufen wird
    // THEN wird kein Löschvorgang im Repository aufgerufen
    @Test
    void deleteById_WhenNullId_DoesNothing() {
        persistenceService.deleteById(null);

        verify(documentRepository, never()).existsById(any());
        verify(documentRepository, never()).deleteById(any());
    }

    // GIVEN eine existierende Dokument-ID
    // WHEN existsById aufgerufen wird
    // THEN wird true zurückgegeben
    @Test
    void existsById_WhenExists_ReturnsTrue() {
        when(documentRepository.existsById(1L)).thenReturn(true);

        assertTrue(persistenceService.existsById(1L));
        verify(documentRepository, times(1)).existsById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN existsById aufgerufen wird
    // THEN wird false zurückgegeben
    @Test
    void existsById_WhenNotExists_ReturnsFalse() {
        when(documentRepository.existsById(99L)).thenReturn(false);

        assertFalse(persistenceService.existsById(99L));
        verify(documentRepository, times(1)).existsById(99L);
    }

    // GIVEN eine Null-Dokument-ID
    // WHEN existsById aufgerufen wird
    // THEN wird false zurückgegeben ohne Repository-Aufruf
    @Test
    void existsById_WhenNullId_ReturnsFalse() {
        assertFalse(persistenceService.existsById(null));
        verify(documentRepository, never()).existsById(any());
    }
}
