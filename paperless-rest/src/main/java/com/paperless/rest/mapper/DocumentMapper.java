package com.paperless.rest.mapper;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dto.DocumentDto;
import com.paperless.rest.service.model.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper decoupling DTO (Presentation), DocumentModel (BL), and DocumentEntity (DAL).
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // DTO <-> BL Model
    DocumentDto toDto(DocumentModel model);
    DocumentModel toModel(DocumentDto dto);
    List<DocumentDto> toDtoList(List<DocumentModel> models);

    // BL Model <-> DAL Entity
    DocumentEntity toEntity(DocumentModel model);

    @Mapping(target = "tags", ignore = true)
    DocumentModel toModel(DocumentEntity entity);

    List<DocumentModel> toModelList(List<DocumentEntity> entities);

    void updateEntityFromModel(DocumentModel model, @MappingTarget DocumentEntity entity);
}
