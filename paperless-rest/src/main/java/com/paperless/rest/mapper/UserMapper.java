package com.paperless.rest.mapper;

import com.paperless.rest.dto.UserDto;
import com.paperless.rest.service.model.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserModel model);
    UserModel toModel(UserDto dto);
}
