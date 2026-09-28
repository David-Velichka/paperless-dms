package com.paperless.rest.mapper;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dto.DocumentDto;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {

    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    // GIVEN ein DocumentModel
    // WHEN toDto aufgerufen wird
    // THEN wird ein identisches DocumentDto erzeugt
    @Test
    void toDto_MapsAllFields() {
        DocumentModel model = DocumentModel.builder()
                .id(1L)
                .title("Invoice")
                .originalFilename("invoice.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .currentStatus(DocumentStatus.RECEIVED)
                .build();

        DocumentDto dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Invoice", dto.getTitle());
        assertEquals(DocumentStatus.RECEIVED, dto.getCurrentStatus());
    }

    // GIVEN ein DocumentDto
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches DocumentModel erzeugt
    @Test
    void toModel_MapsAllFields() {
        DocumentDto dto = DocumentDto.builder()
                .id(2L)
                .title("Contract")
                .currentStatus(DocumentStatus.PROCESSING)
                .build();

        DocumentModel model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals(2L, model.getId());
        assertEquals(DocumentStatus.PROCESSING, model.getCurrentStatus());
    }

    // GIVEN ein DocumentModel
    // WHEN toEntity aufgerufen wird
    // THEN wird eine identische DocumentEntity erzeugt
    @Test
    void toEntity_MapsAllFields() {
        DocumentModel model = DocumentModel.builder()
                .id(3L)
                .title("Receipt")
                .currentStatus(DocumentStatus.COMPLETED)
                .build();

        DocumentEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(3L, entity.getId());
        assertEquals(DocumentStatus.COMPLETED, entity.getCurrentStatus());
    }

    // GIVEN eine DocumentEntity
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches DocumentModel erzeugt
    @Test
    void toModel_FromEntity_MapsAllFields() {
        DocumentEntity entity = DocumentEntity.builder()
                .id(4L)
                .title("Report")
                .currentStatus(DocumentStatus.REVIEW_PENDING)
                .build();

        DocumentModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(4L, model.getId());
        assertEquals(DocumentStatus.REVIEW_PENDING, model.getCurrentStatus());
    }

    // GIVEN Listen von Modellen und Entitäten
    // WHEN Listen-Mapping-Methoden aufgerufen werden
    // THEN werden entsprechende Listen zurückgegeben
    @Test
    void listMappings_WorkCorrectly() {
        DocumentModel model = DocumentModel.builder().id(5L).build();
        DocumentEntity entity = DocumentEntity.builder().id(6L).build();

        List<DocumentDto> dtoList = mapper.toDtoList(List.of(model));
        List<DocumentModel> modelList = mapper.toModelList(List.of(entity));

        assertEquals(1, dtoList.size());
        assertEquals(5L, dtoList.get(0).getId());
        assertEquals(1, modelList.size());
        assertEquals(6L, modelList.get(0).getId());
    }
}
