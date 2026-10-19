package com.paperless.rest.config;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingConfigTest {

    private final LoggingConfig loggingConfig = new LoggingConfig();

    // GIVEN ein gültiger InjectionPoint für eine Zielklasse
    // WHEN die logger-Bean-Methode aufgerufen wird
    // THEN wird eine gültige SLF4J-Logger-Instanz für den entsprechenden Klassennamen bereitgestellt
    @Test
    void logger_WithMethodParameterInjectionPoint_ReturnsCorrectLogger() throws Exception {
        InjectionPoint injectionPoint = mock(InjectionPoint.class);
        Method dummyMethod = LoggingConfigTest.class.getDeclaredMethod("dummyMethod", Logger.class);
        MethodParameter methodParameter = new MethodParameter(dummyMethod, 0);

        when(injectionPoint.getMethodParameter()).thenReturn(methodParameter);

        Logger logger = loggingConfig.logger(injectionPoint);

        assertNotNull(logger);
        assertEquals(LoggingConfigTest.class.getName(), logger.getName());
    }

    // GIVEN ein InjectionPoint ohne spezifische Methoden- oder Feld-Parameter
    // WHEN die logger-Bean-Methode aufgerufen wird
    // THEN wird ein Standard-Logger für die LoggingConfig-Klasse zurückgegeben
    @Test
    void logger_WithFallbackInjectionPoint_ReturnsDefaultLogger() {
        InjectionPoint injectionPoint = mock(InjectionPoint.class);
        when(injectionPoint.getMethodParameter()).thenReturn(null);
        when(injectionPoint.getField()).thenReturn(null);

        Logger logger = loggingConfig.logger(injectionPoint);

        assertNotNull(logger);
        assertEquals(LoggingConfig.class.getName(), logger.getName());
    }

    private void dummyMethod(Logger log) {
    }
}

