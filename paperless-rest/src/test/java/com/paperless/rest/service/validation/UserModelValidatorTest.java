package com.paperless.rest.service.validation;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.service.model.UserModel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelValidatorTest {

    private UserModelValidator userModelValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        userModelValidator = new UserModelValidator(validator);
    }

    // GIVEN ein gültiges UserModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird keine Exception geworfen und die Validierung ist erfolgreich
    @Test
    void validateOrThrow_WithValidModel_Succeeds() {
        UserModel user = new UserModel("admin", "ADMIN");

        assertDoesNotThrow(() -> userModelValidator.validateOrThrow(user));
        assertTrue(userModelValidator.validate(user).isEmpty());
    }

    // GIVEN ein Null-UserModel
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithNullModel_ThrowsBusinessLayerException() {
        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> userModelValidator.validateOrThrow(null));
        assertEquals("User model must not be null", ex.getMessage());
    }

    // GIVEN ein UserModel mit leerem Benutzernamen
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithBlankUsername_ThrowsBusinessLayerException() {
        UserModel user = new UserModel("   ", "ADMIN");

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> userModelValidator.validateOrThrow(user));
        assertTrue(ex.getMessage().contains("Username must not be blank"));
    }

    // GIVEN ein UserModel mit leerer Rolle
    // WHEN validateOrThrow aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void validateOrThrow_WithBlankRole_ThrowsBusinessLayerException() {
        UserModel user = new UserModel("admin", "   ");

        BusinessLayerException ex = assertThrows(BusinessLayerException.class,
                () -> userModelValidator.validateOrThrow(user));
        assertTrue(ex.getMessage().contains("Role must not be blank"));
    }
}

