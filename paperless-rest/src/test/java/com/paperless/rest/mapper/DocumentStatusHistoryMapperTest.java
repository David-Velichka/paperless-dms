package com.paperless.rest.mapper;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dto.DocumentStatusHistoryDto;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStatusHistoryMapperTest {

    private final DocumentStatusHistoryMapper mapper = Mappers.getMapper(DocumentStatusHistoryMapper.class);

    // GIVEN ein DocumentStatusHistoryModel
    // WHEN toDto aufgerufen wird
    // THEN wird ein identisches DocumentStatusHistoryDto erzeugt
    @Test
    void toDto_MapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .id(1L)
                .documentId(10L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("worker")
                .timestamp(now)
                .comment("Processing")
                .build();

        DocumentStatusHistoryDto dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getDocumentId());
        assertEquals(DocumentStatus.PROCESSING, dto.getStatus());
        assertEquals(DocumentStatus.RECEIVED, dto.getPreviousStatus());
        assertEquals("worker", dto.getChangedBy());
        assertEquals(now, dto.getTimestamp());
        assertEquals("Processing", dto.getComment());
    }

    // GIVEN ein DocumentStatusHistoryDto
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches DocumentStatusHistoryModel erzeugt
    @Test
    void toModel_MapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatusHistoryDto dto = DocumentStatusHistoryDto.builder()
                .id(2L)
                .documentId(20L)
                .status(DocumentStatus.COMPLETED)
                .previousStatus(DocumentStatus.REVIEW_PENDING)
                .changedBy("reviewer")
                .timestamp(now)
                .comment("Approved")
                .build();

        DocumentStatusHistoryModel model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals(2L, model.getId());
        assertEquals(DocumentStatus.COMPLETED, model.getStatus());
    }

    // GIVEN ein DocumentStatusHistoryModel
    // WHEN toEntity aufgerufen wird
    // THEN wird eine identische DocumentStatusHistoryEntity erzeugt
    @Test
    void toEntity_MapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder()
                .id(3L)
                .documentId(30L)
                .status(DocumentStatus.REJECTED)
                .previousStatus(DocumentStatus.REVIEW_PENDING)
                .changedBy("admin")
                .timestamp(now)
                .comment("Declined")
                .build();

        DocumentStatusHistoryEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(3L, entity.getId());
        assertEquals(DocumentStatus.REJECTED, entity.getStatus());
    }

    // GIVEN eine DocumentStatusHistoryEntity
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches DocumentStatusHistoryModel erzeugt
    @Test
    void toModel_FromEntity_MapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatusHistoryEntity entity = DocumentStatusHistoryEntity.builder()
                .id(4L)
                .documentId(40L)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .changedBy("system")
                .timestamp(now)
                .comment("Initial")
                .build();

        DocumentStatusHistoryModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(4L, model.getId());
        assertEquals(DocumentStatus.RECEIVED, model.getStatus());
    }

    // GIVEN Listen von Modellen und Entitäten
    // WHEN Listen-Mapping-Methoden aufgerufen werden
    // THEN werden entsprechende Listen zurückgegeben
    @Test
    void listMappings_WorkCorrectly() {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder().id(5L).build();
        DocumentStatusHistoryEntity entity = DocumentStatusHistoryEntity.builder().id(6L).build();

        List<DocumentStatusHistoryDto> dtoList = mapper.toDtoList(List.of(model));
        List<DocumentStatusHistoryModel> modelList = mapper.toModelList(List.of(entity));

        assertEquals(1, dtoList.size());
        assertEquals(5L, dtoList.get(0).getId());
        assertEquals(1, modelList.size());
        assertEquals(6L, modelList.get(0).getId());
    }
}
