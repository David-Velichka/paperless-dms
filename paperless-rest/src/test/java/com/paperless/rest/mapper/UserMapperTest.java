package com.paperless.rest.mapper;

import com.paperless.rest.dto.UserDto;
import com.paperless.rest.service.model.UserModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    // GIVEN ein UserModel
    // WHEN toDto aufgerufen wird
    // THEN wird ein identisches UserDto erzeugt
    @Test
    void toDto_MapsAllFields() {
        UserModel model = new UserModel("admin", "ADMIN");

        UserDto dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals("admin", dto.username());
        assertEquals("ADMIN", dto.role());
    }

    // GIVEN ein UserDto
    // WHEN toModel aufgerufen wird
    // THEN wird ein identisches UserModel erzeugt
    @Test
    void toModel_MapsAllFields() {
        UserDto dto = new UserDto("user", "USER");

        UserModel model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals("user", model.getUsername());
        assertEquals("USER", model.getRole());
    }
}
