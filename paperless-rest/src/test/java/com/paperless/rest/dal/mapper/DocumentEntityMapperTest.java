package com.paperless.rest.dal.mapper;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentEntityMapperTest {

    private final DocumentEntityMapper mapper = Mappers.getMapper(DocumentEntityMapper.class);

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

    // GIVEN eine Liste von DocumentEntity Instanzen
    // WHEN toModelList aufgerufen wird
    // THEN wird eine entsprechende Liste von DocumentModel zurückgegeben
    @Test
    void toModelList_WorksCorrectly() {
        DocumentEntity entity = DocumentEntity.builder().id(6L).title("Entity Doc").build();

        List<DocumentModel> modelList = mapper.toModelList(List.of(entity));

        assertEquals(1, modelList.size());
        assertEquals(6L, modelList.get(0).getId());
    }
}
