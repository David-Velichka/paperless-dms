package com.paperless.rest.mapper;

import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentResponseDto;
import com.paperless.rest.dto.DocumentUpdateDto;
import com.paperless.rest.service.model.DocumentModel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Presentation-tier MapStruct mapper converting between DTOs and BL DocumentModel.
 * Pure functions only; no in-place mutation.
 */
@Mapper(componentModel = "spring")
public interface DocumentDtoMapper {

    DocumentResponseDto toResponseDto(DocumentModel model);

    List<DocumentResponseDto> toResponseDtoList(List<DocumentModel> models);

    DocumentModel toModel(DocumentCreateDto dto);

    DocumentModel toModel(DocumentUpdateDto dto);
}
