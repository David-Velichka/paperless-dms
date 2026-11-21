package com.paperless.ocr.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    // Same durable queue as declared by paperless-rest, so the worker can start first
    @Bean
    public Queue ocrQueue(RabbitMqProperties properties) {
        return new Queue(properties.ocrQueue(), true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Invalid messages can never succeed, so they are rejected at once instead of being retried
    @Bean
    public RabbitListenerRetrySettingsCustomizer noRetryForInvalidMessages() {
        return settings -> settings.setExceptionExcludes(List.of(AmqpRejectAndDontRequeueException.class));
    }
}
