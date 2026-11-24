package com.paperless.rest.mapper;

import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentResponseDto;
import com.paperless.rest.dto.DocumentUpdateDto;
import com.paperless.rest.service.model.DocumentModel;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Presentation-tier MapStruct mapper converting between DTOs and BL DocumentModel.
 * Pure functions only; no in-place mutation.
 */
@Mapper(componentModel = "spring")
public interface DocumentDtoMapper {

    DocumentResponseDto toResponseDto(DocumentModel model);

    // List responses leave out the OCR text, it can be very long; the single document keeps it
    @Named("toListItemDto")
    @Mapping(target = "ocrText", ignore = true)
    DocumentResponseDto toListItemDto(DocumentModel model);

    @IterableMapping(qualifiedByName = "toListItemDto")
    List<DocumentResponseDto> toResponseDtoList(List<DocumentModel> models);

    DocumentModel toModel(DocumentCreateDto dto);

    DocumentModel toModel(DocumentUpdateDto dto);
}
