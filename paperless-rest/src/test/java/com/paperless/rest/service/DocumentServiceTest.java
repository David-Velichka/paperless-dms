package com.paperless.rest.service;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.impl.DocumentServiceImpl;
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
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private DocumentStatusHistoryService documentStatusHistoryService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private DocumentModel sampleModel;
    private DocumentEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleModel = DocumentModel.builder()
                .id(1L)
                .title("Test Invoice")
                .build();

        sampleEntity = DocumentEntity.builder()
                .id(1L)
                .title("Test Invoice")
                .build();
    }

    // GIVEN ein gültiges DocumentModel
    // WHEN save im Service aufgerufen wird
    // THEN wird das Modell über das Repository gespeichert und zurückgegeben
    @Test
    void save_WithValidModel_ReturnsSavedModel() {
        when(documentMapper.toEntity(sampleModel)).thenReturn(sampleEntity);
        when(documentRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(documentMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        DocumentModel result = documentService.save(sampleModel);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Invoice", result.getTitle());
        verify(documentRepository, times(1)).save(sampleEntity);
    }

    // GIVEN ein Null-DocumentModel
    // WHEN save im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void save_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.save(null));
        verify(documentRepository, never()).save(any());
    }

    // GIVEN ein DocumentModel ohne Titel
    // WHEN save im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void save_WithBlankTitle_ThrowsBusinessLayerException() {
        DocumentModel invalid = DocumentModel.builder().title("   ").build();

        assertThrows(BusinessLayerException.class, () -> documentService.save(invalid));
        verify(documentRepository, never()).save(any());
    }

    // GIVEN eine gültige Dokument-ID für ein existierendes Dokument
    // WHEN findById im Service aufgerufen wird
    // THEN wird das gemappte DocumentModel zurückgegeben
    @Test
    void findById_WhenExists_ReturnsDocumentModel() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(documentMapper.toModel(sampleEntity)).thenReturn(sampleModel);

        DocumentModel result = documentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Invoice", result.getTitle());
        verify(documentRepository, times(1)).findById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID
    // WHEN findById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void findById_WhenNotFound_ThrowsBusinessLayerException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class, () -> documentService.findById(99L));
        verify(documentRepository, times(1)).findById(99L);
    }

    // GIVEN vorhandene Dokumente in der Datenbank
    // WHEN findAll im Service aufgerufen wird
    // THEN wird eine Liste von DocumentModels zurückgegeben
    @Test
    void findAll_ReturnsAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(documentMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<DocumentModel> results = documentService.findAll();

        assertEquals(1, results.size());
        assertEquals("Test Invoice", results.getFirst().getTitle());
        verify(documentRepository, times(1)).findAll();
    }

    // GIVEN eine gültige Suchanfrage
    // WHEN search im Service aufgerufen wird
    // THEN werden passende Dokumente über das Repository gesucht und zurückgegeben
    @Test
    void search_WithValidQuery_ReturnsMatchingDocuments() {
        when(documentRepository.findByTitleContainingIgnoreCase("Invoice")).thenReturn(List.of(sampleEntity));
        when(documentMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<DocumentModel> results = documentService.search("Invoice");

        assertEquals(1, results.size());
        verify(documentRepository, times(1)).findByTitleContainingIgnoreCase("Invoice");
    }

    // GIVEN eine leere Suchanfrage
    // WHEN search im Service aufgerufen wird
    // THEN werden alle Dokumente zurückgegeben
    @Test
    void search_WithBlankQuery_ReturnsAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(sampleEntity));
        when(documentMapper.toModelList(List.of(sampleEntity))).thenReturn(List.of(sampleModel));

        List<DocumentModel> results = documentService.search("   ");

        assertEquals(1, results.size());
        verify(documentRepository, times(1)).findAll();
    }

    // GIVEN eine existierende Dokument-ID
    // WHEN deleteById im Service aufgerufen wird
    // THEN wird das Dokument im Repository gelöscht
    @Test
    void deleteById_WhenExists_DeletesDocument() {
        when(documentRepository.existsById(1L)).thenReturn(true);

        documentService.deleteById(1L);

        verify(documentRepository, times(1)).deleteById(1L);
    }

    // GIVEN eine nicht existierende Dokument-ID zum Löschen
    // WHEN deleteById im Service aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void deleteById_WhenNotExists_ThrowsBusinessLayerException() {
        when(documentRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessLayerException.class, () -> documentService.deleteById(99L));
        verify(documentRepository, never()).deleteById(anyLong());
    }

    // GIVEN ein DocumentModel und binäre Dateidaten
    // WHEN uploadDocument aufgerufen wird
    // THEN wird die Entität persistiert und das gespeicherte Modell zurückgegeben
    @Test
    void uploadDocument_SavesEntityAndReturnsModel() {
        DocumentModel inputModel = DocumentModel.builder()
                .title("Upload Test")
                .originalFilename("test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .build();

        DocumentEntity entityToSave = DocumentEntity.builder()
                .title("Upload Test")
                .originalFilename("test.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .build();

        DocumentEntity savedEntity = DocumentEntity.builder()
                .id(10L)
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
                .build();

        when(documentMapper.toEntity(inputModel)).thenReturn(entityToSave);
        when(documentRepository.save(entityToSave)).thenReturn(savedEntity);
        when(documentMapper.toModel(savedEntity)).thenReturn(expectedModel);

        DocumentModel result = documentService.uploadDocument(inputModel, "dummy content".getBytes());

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Upload Test", result.getTitle());
        verify(documentRepository, times(1)).save(entityToSave);
    }

    // GIVEN ein Null-DocumentModel beim Upload
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithNullModel_ThrowsBusinessLayerException() {
        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(null, "content".getBytes()));
        verify(documentRepository, never()).save(any());
    }

    // GIVEN ein DocumentModel mit leerem Dateiinhalt
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithEmptyContent_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder().title("Test").build();

        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(model, new byte[0]));
        verify(documentRepository, never()).save(any());
    }

    // GIVEN ein DocumentModel mit leerem Titel beim Upload
    // WHEN uploadDocument aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void uploadDocument_WithBlankTitle_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder().title(" ").build();

        assertThrows(BusinessLayerException.class, () -> documentService.uploadDocument(model, "content".getBytes()));
        verify(documentRepository, never()).save(any());
    }
}
