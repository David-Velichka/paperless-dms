package com.paperless.rest.dal.mapper;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.service.model.DocumentModel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Data Access Layer (DAL) MapStruct mapper converting between BL DocumentModel and DAL DocumentEntity.
 * Pure functions only; no in-place mutation.
 */
@Mapper(componentModel = "spring")
public interface DocumentEntityMapper {

    DocumentEntity toEntity(DocumentModel model);

    DocumentModel toModel(DocumentEntity entity);

    List<DocumentModel> toModelList(List<DocumentEntity> entities);
}
