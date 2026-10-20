package com.paperless.rest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elasticsearch")
public record ElasticsearchConfig(String uris) {

    public String getElasticsearchUri() {
        return uris;
    }
}
