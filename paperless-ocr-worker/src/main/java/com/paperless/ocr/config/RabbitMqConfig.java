package com.paperless.ocr.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.rabbitmq.autoconfigure.RabbitListenerRetrySettingsCustomizer;
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

    // Exchange, result queue and binding are declared identically in paperless-rest, so either app can start first
    @Bean
    public DirectExchange documentExchange(RabbitMqProperties properties) {
        return new DirectExchange(properties.exchange());
    }

    @Bean
    public Queue ocrResultQueue(RabbitMqProperties properties) {
        return new Queue(properties.ocrResultQueue(), true);
    }

    @Bean
    public Binding ocrResultBinding(Queue ocrResultQueue, DirectExchange documentExchange,
                                   RabbitMqProperties properties) {
        return BindingBuilder.bind(ocrResultQueue).to(documentExchange).with(properties.ocrResultRoutingKey());
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
