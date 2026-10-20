package com.paperless.rest.mapper;

import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentDto;
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

    // GIVEN Listen von DocumentModel Instanzen
    // WHEN Listen-Mapping-Methoden aufgerufen werden
    // THEN werden entsprechende Listen zurückgegeben
    @Test
    void listMappings_WorkCorrectly() {
        DocumentModel model = DocumentModel.builder().id(5L).title("List Doc").build();

        List<DocumentDto> dtoList = mapper.toDtoList(List.of(model));
        List<DocumentResponseDto> responseList = mapper.toResponseDtoList(List.of(model));

        assertEquals(1, dtoList.size());
        assertEquals(5L, dtoList.get(0).getId());
        assertEquals(1, responseList.size());
        assertEquals(5L, responseList.get(0).getId());
    }
}
