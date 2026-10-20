package com.paperless.rest.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElasticsearchConfigTest {

    // GIVEN ein gültiger URI-String für Elasticsearch
    // WHEN ein ElasticsearchConfig Record instanziiert wird
    // THEN liefert uris und getElasticsearchUri den konfigurierten Wert zurück
    @Test
    void elasticsearchConfig_StoresAndReturnsUris() {
        ElasticsearchConfig config = new ElasticsearchConfig("http://elasticsearch:9200");

        assertEquals("http://elasticsearch:9200", config.uris());
        assertEquals("http://elasticsearch:9200", config.getElasticsearchUri());
    }
}

