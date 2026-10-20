package com.paperless.rest.dal.mapper;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.service.model.DocumentStatus;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStatusHistoryEntityMapperTest {

    private final DocumentStatusHistoryEntityMapper mapper = Mappers.getMapper(DocumentStatusHistoryEntityMapper.class);

    // GIVEN ein DocumentStatusHistoryModel
    // WHEN toEntity aufgerufen wird
    // THEN wird eine identische DocumentStatusHistoryEntity erzeugt
    @Test
    void toEntity_MapsAllFields() {
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

        DocumentStatusHistoryEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(10L, entity.getDocumentId());
        assertEquals(DocumentStatus.PROCESSING, entity.getStatus());
        assertEquals(DocumentStatus.RECEIVED, entity.getPreviousStatus());
        assertEquals("worker", entity.getChangedBy());
        assertEquals(now, entity.getTimestamp());
        assertEquals("Processing", entity.getComment());
    }

    // GIVEN eine DocumentStatusHistoryEntity
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches DocumentStatusHistoryModel erzeugt
    @Test
    void toModel_FromEntity_MapsAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentStatusHistoryEntity entity = DocumentStatusHistoryEntity.builder()
                .id(2L)
                .documentId(20L)
                .status(DocumentStatus.COMPLETED)
                .previousStatus(DocumentStatus.REVIEW_PENDING)
                .changedBy("reviewer")
                .timestamp(now)
                .comment("Approved")
                .build();

        DocumentStatusHistoryModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(2L, model.getId());
        assertEquals(20L, model.getDocumentId());
        assertEquals(DocumentStatus.COMPLETED, model.getStatus());
        assertEquals(DocumentStatus.REVIEW_PENDING, model.getPreviousStatus());
        assertEquals("reviewer", model.getChangedBy());
        assertEquals(now, model.getTimestamp());
        assertEquals("Approved", model.getComment());
    }

    // GIVEN eine Liste von DocumentStatusHistoryEntity Instanzen
    // WHEN toModelList aufgerufen wird
    // THEN wird eine entsprechende Liste von DocumentStatusHistoryModel zurückgegeben
    @Test
    void toModelList_WorksCorrectly() {
        DocumentStatusHistoryEntity entity = DocumentStatusHistoryEntity.builder()
                .id(3L)
                .documentId(30L)
                .status(DocumentStatus.RECEIVED)
                .build();

        List<DocumentStatusHistoryModel> modelList = mapper.toModelList(List.of(entity));

        assertEquals(1, modelList.size());
        assertEquals(3L, modelList.get(0).getId());
        assertEquals(30L, modelList.get(0).getDocumentId());
    }
}
