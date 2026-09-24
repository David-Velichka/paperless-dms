package com.paperless.rest.service;

import com.paperless.rest.dal.entity.CustomEntity;
import com.paperless.rest.dal.repository.CustomEntityRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.CustomEntityMapper;
import com.paperless.rest.service.impl.CustomEntityServiceImpl;
import com.paperless.rest.service.model.CustomEntityModel;
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
class CustomEntityServiceTest {

    @Mock
    private CustomEntityRepository customEntityRepository;

    @Mock
    private CustomEntityMapper customEntityMapper;

    @InjectMocks
    private CustomEntityServiceImpl customEntityService;

    private CustomEntityModel sampleModel;
    private CustomEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleModel = CustomEntityModel.builder()
                .id(1L)
                .name("Important Tag")
                .description("Tag for critical documents")
                .documentId(50L)
                .build();

        sampleEntity = CustomEntity.builder()
                .id(1L)
                .name("Important Tag")
                .description("Tag for critical documents")
                .documentId(50L)
                .build();
    }

    // GIVEN ein gültiges CustomEntityModel
    // WHEN create im Service aufgerufen wird
    // THEN wird die Entität gespeichert und als gemapptes Modell zurückgegeben
    @Test
    void create_SavesAndReturnsModel() {
        when(customEntityMapper.toEntity(sampleModel)).thenReturn(sampleEntity);
        when(customEntityRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(customEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        CustomEntityModel created = customEntityService.create(sampleModel);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Important Tag", created.getName());
        verify(customEntityRepository, times(1)).save(sampleEntity);
    }

    // GIVEN ein Null-CustomEntityModel
    // WHEN create im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void create_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> customEntityService.create(null));
        verify(customEntityRepository, never()).save(any());
    }

    // GIVEN ein CustomEntityModel mit leerem Namen
    // WHEN create im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void create_WithBlankName_ThrowsBusinessLayerException() {
        CustomEntityModel invalid = CustomEntityModel.builder().name("   ").build();

        assertThrows(BusinessLayerException.class, () -> customEntityService.create(invalid));
        verify(customEntityRepository, never()).save(any());
    }

    // GIVEN eine gültige ID einer existierenden benutzerdefinierten Entität
    // WHEN findById im Service aufgerufen wird
    // THEN wird das gemappte Modell zurückgegeben
    @Test
    void findById_WhenExists_ReturnsModel() {
        when(customEntityRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(customEntityMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        CustomEntityModel result = customEntityService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customEntityRepository, times(1)).findById(1L);
    }

    // GIVEN eine Null-ID
    // WHEN findById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> customEntityService.findById(null));
        verify(customEntityRepository, never()).findById(any());
    }

    // GIVEN eine nicht existierende Entitäts-ID
    // WHEN findById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WhenNotFound_ThrowsBusinessLayerException() {
        when(customEntityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class, () -> customEntityService.findById(99L));
        verify(customEntityRepository, times(1)).findById(99L);
    }

    // GIVEN eine Dokumenten-ID mit zugeordneten benutzerdefinierten Entitäten
    // WHEN findByDocumentId aufgerufen wird
    // THEN wird die Liste der gemappten Modelle zurückgegeben
    @Test
    void findByDocumentId_ReturnsModelList() {
        when(customEntityRepository.findByDocumentId(50L)).thenReturn(List.of(sampleEntity));
        when(customEntityMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<CustomEntityModel> results = customEntityService.findByDocumentId(50L);

        assertEquals(1, results.size());
        assertEquals("Important Tag", results.getFirst().getName());
        verify(customEntityRepository, times(1)).findByDocumentId(50L);
    }

    // GIVEN eine Null-Dokument-ID
    // WHEN findByDocumentId aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findByDocumentId_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> customEntityService.findByDocumentId(null));
        verify(customEntityRepository, never()).findByDocumentId(any());
    }

    // GIVEN existierende benutzerdefinierte Entitäten
    // WHEN findAll aufgerufen wird
    // THEN werden alle Entitäten als Modellliste zurückgegeben
    @Test
    void findAll_ReturnsAllModels() {
        when(customEntityRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(customEntityMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<CustomEntityModel> results = customEntityService.findAll();

        assertEquals(1, results.size());
        verify(customEntityRepository, times(1)).findAll();
    }

    // GIVEN eine existierende benutzerdefinierte Entität
    // WHEN deleteById im Service aufgerufen wird
    // THEN wird die Entität über das Repository gelöscht
    @Test
    void deleteById_WhenExists_DeletesEntity() {
        when(customEntityRepository.existsById(1L)).thenReturn(true);

        customEntityService.deleteById(1L);

        verify(customEntityRepository, times(1)).deleteById(1L);
    }

    // GIVEN eine Null-ID zum Löschen
    // WHEN deleteById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void deleteById_WithNullId_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> customEntityService.deleteById(null));
        verify(customEntityRepository, never()).deleteById(any());
    }

    // GIVEN eine nicht existierende benutzerdefinierte Entitäts-ID
    // WHEN deleteById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void deleteById_WhenNotExists_ThrowsBusinessLayerException() {
        when(customEntityRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessLayerException.class, () -> customEntityService.deleteById(99L));
        verify(customEntityRepository, never()).deleteById(anyLong());
    }
}
