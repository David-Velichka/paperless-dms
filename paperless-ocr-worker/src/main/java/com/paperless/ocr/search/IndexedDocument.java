package com.paperless.ocr.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Entry of the Elasticsearch index, the id is the document id from PostgreSQL.
 * The mapping is created by Elasticsearch with the first save: both strings become analyzed text fields.
 */
@Document(indexName = "paperless-documents")
public record IndexedDocument(@Id String id, String originalFilename, String content) {
}
