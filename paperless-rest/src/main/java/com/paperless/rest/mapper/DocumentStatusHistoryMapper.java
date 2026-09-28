package com.paperless.rest.mapper;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dto.DocumentStatusHistoryDto;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper decoupling DocumentStatusHistory across presentation, business, and persistence layers.
 */
@Mapper(componentModel = "spring")
public interface DocumentStatusHistoryMapper {

    // DTO <-> BL Model
    DocumentStatusHistoryDto toDto(DocumentStatusHistoryModel model);
    DocumentStatusHistoryModel toModel(DocumentStatusHistoryDto dto);
    List<DocumentStatusHistoryDto> toDtoList(List<DocumentStatusHistoryModel> models);

    // BL Model <-> DAL Entity
    DocumentStatusHistoryEntity toEntity(DocumentStatusHistoryModel model);
    DocumentStatusHistoryModel toModel(DocumentStatusHistoryEntity entity);
    List<DocumentStatusHistoryModel> toModelList(List<DocumentStatusHistoryEntity> entities);
}
