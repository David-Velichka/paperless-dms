package com.paperless.rest.mapper;

import com.paperless.rest.dto.DocumentStatusHistoryDto;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper decoupling DocumentStatusHistory across presentation and business layers.
 */
@Mapper(componentModel = "spring")
public interface DocumentStatusHistoryMapper {

    // DTO <-> BL Model
    DocumentStatusHistoryDto toDto(DocumentStatusHistoryModel model);
    DocumentStatusHistoryModel toModel(DocumentStatusHistoryDto dto);
    List<DocumentStatusHistoryDto> toDtoList(List<DocumentStatusHistoryModel> models);
}
