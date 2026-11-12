package com.paperless.rest.mapper;

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
                .timestamp(now)
                .comment("Processing")
                .build();

        DocumentStatusHistoryDto dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getDocumentId());
        assertEquals(DocumentStatus.PROCESSING, dto.getStatus());
        assertEquals(DocumentStatus.RECEIVED, dto.getPreviousStatus());
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
                .timestamp(now)
                .comment("Approved")
                .build();

        DocumentStatusHistoryModel model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals(2L, model.getId());
        assertEquals(DocumentStatus.COMPLETED, model.getStatus());
    }

    // GIVEN eine Liste von Modellen
    // WHEN die Listen-Mapping-Methode aufgerufen wird
    // THEN wird eine entsprechende Liste von DTOs zurückgegeben
    @Test
    void listMappings_WorkCorrectly() {
        DocumentStatusHistoryModel model = DocumentStatusHistoryModel.builder().id(5L).build();

        List<DocumentStatusHistoryDto> dtoList = mapper.toDtoList(List.of(model));

        assertEquals(1, dtoList.size());
        assertEquals(5L, dtoList.get(0).getId());
    }
}
