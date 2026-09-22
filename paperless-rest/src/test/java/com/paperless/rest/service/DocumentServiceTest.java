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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

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

    // GIVEN a valid document ID
    // WHEN findById is called
    // THEN return mapped DocumentModel
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

    // GIVEN a non-existent document ID
    // WHEN findById is called
    // THEN throw BusinessLayerException
    @Test
    void findById_WhenNotFound_ThrowsBusinessLayerException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessLayerException.class, () -> documentService.findById(99L));
        verify(documentRepository, times(1)).findById(99L);
    }

    // GIVEN a DocumentModel and raw file bytes
    // WHEN uploadDocument is invoked
    // THEN persist entity via repository and return mapped model with assigned ID
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
}
