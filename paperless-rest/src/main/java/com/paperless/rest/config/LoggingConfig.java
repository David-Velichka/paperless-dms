package com.paperless.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Dependency Injection configuration for SLF4J Logger.
 * Enables automatic constructor and parameter injection of org.slf4j.Logger across components.
 */
@Configuration
public class LoggingConfig {

    @Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint injectionPoint) {
        if (injectionPoint.getMethodParameter() != null) {
            return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
        }
        if (injectionPoint.getField() != null) {
            return LoggerFactory.getLogger(injectionPoint.getField().getDeclaringClass());
        }
        return LoggerFactory.getLogger(LoggingConfig.class);
    }
}

