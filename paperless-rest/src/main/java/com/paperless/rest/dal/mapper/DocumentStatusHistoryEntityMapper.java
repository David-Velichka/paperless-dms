package com.paperless.rest.dal.mapper;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Data Access Layer (DAL) MapStruct mapper converting between BL DocumentStatusHistoryModel and DAL DocumentStatusHistoryEntity.
 * Pure functions only; no in-place mutation.
 */
@Mapper(componentModel = "spring")
public interface DocumentStatusHistoryEntityMapper {

    DocumentStatusHistoryEntity toEntity(DocumentStatusHistoryModel model);

    DocumentStatusHistoryModel toModel(DocumentStatusHistoryEntity entity);

    List<DocumentStatusHistoryModel> toModelList(List<DocumentStatusHistoryEntity> entities);
}
