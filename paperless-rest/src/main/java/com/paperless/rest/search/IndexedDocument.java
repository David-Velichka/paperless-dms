package com.paperless.rest.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Entry of the Elasticsearch index, the id is the document id from PostgreSQL.
 * Written by the OCR worker, the REST server only searches and deletes entries.
 * Must stay field-identical with the class in the worker.
 */
@Document(indexName = "paperless-documents")
public record IndexedDocument(@Id String id, String originalFilename, String content) {
}
