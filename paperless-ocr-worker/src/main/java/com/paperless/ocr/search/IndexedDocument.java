package com.paperless.ocr.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

/**
 * Entry of the Elasticsearch index, the id is the document id from PostgreSQL.
 * The mapping is created by Elasticsearch with the first save: both strings become analyzed text fields.
 * No class name is written into the entry, because the search side reads it with its own class.
 */
@Document(indexName = "paperless-documents", writeTypeHint = WriteTypeHint.FALSE)
public record IndexedDocument(@Id String id, String originalFilename, String content) {
}
