package com.paperless.rest.mapper;

import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentResponseDto;
import com.paperless.rest.dto.DocumentUpdateDto;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDtoMapperTest {

    private final DocumentDtoMapper mapper = Mappers.getMapper(DocumentDtoMapper.class);

    // GIVEN ein valides DocumentModel
    // WHEN toResponseDto aufgerufen wird
    // THEN wird ein passendes DocumentResponseDto erzeugt
    @Test
    void toResponseDto_MapsAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        DocumentModel model = DocumentModel.builder()
                .id(1L)
                .version(2L)
                .title("Annual Report")
                .originalFilename("report.pdf")
                .contentType("application/pdf")
                .fileSize(2048L)
                .storagePath("/storage/report.pdf")
                .ocrText("OCR content")
                .summary("Summary text")
                .currentStatus(DocumentStatus.COMPLETED)
                .createdAt(now)
                .modifiedAt(now)
                .build();

        DocumentResponseDto dto = mapper.toResponseDto(model);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getVersion());
        assertEquals("Annual Report", dto.getTitle());
        assertEquals("report.pdf", dto.getOriginalFilename());
        assertEquals("application/pdf", dto.getContentType());
        assertEquals(2048L, dto.getFileSize());
        assertEquals("/storage/report.pdf", dto.getStoragePath());
        assertEquals("OCR content", dto.getOcrText());
        assertEquals("Summary text", dto.getSummary());
        assertEquals(DocumentStatus.COMPLETED, dto.getCurrentStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getModifiedAt());
    }

    // GIVEN ein valides DocumentCreateDto
    // WHEN toModel aufgerufen wird
    // THEN wird ein passendes DocumentModel erzeugt
    @Test
    void toModel_FromCreateDto_MapsFields() {
        DocumentCreateDto createDto = DocumentCreateDto.builder()
                .title("New Invoice")
                .originalFilename("inv.pdf")
                .build();

        DocumentModel model = mapper.toModel(createDto);

        assertNotNull(model);
        assertEquals("New Invoice", model.getTitle());
        assertEquals("inv.pdf", model.getOriginalFilename());
    }

    // GIVEN ein valides DocumentUpdateDto
    // WHEN toModel aufgerufen wird
    // THEN wird ein passendes DocumentModel mit neuem Titel erzeugt
    @Test
    void toModel_FromUpdateDto_MapsFields() {
        DocumentUpdateDto updateDto = DocumentUpdateDto.builder()
                .title("Updated Title")
                .build();

        DocumentModel model = mapper.toModel(updateDto);

        assertNotNull(model);
        assertEquals("Updated Title", model.getTitle());
    }

    // GIVEN Listen von DocumentModel Instanzen
    // WHEN toResponseDtoList aufgerufen wird
    // THEN wird eine entsprechende Liste zurückgegeben
    @Test
    void listMappings_WorkCorrectly() {
        DocumentModel model = DocumentModel.builder().id(5L).title("List Doc").build();

        List<DocumentResponseDto> responseList = mapper.toResponseDtoList(List.of(model));

        assertEquals(1, responseList.size());
        assertEquals(5L, responseList.get(0).getId());
    }
}
