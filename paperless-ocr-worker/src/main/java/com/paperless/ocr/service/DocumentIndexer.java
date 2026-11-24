package com.paperless.ocr.service;

/**
 * Stores the recognized text of a document in the search index.
 * The business layer only knows this interface, Elasticsearch is a detail of the implementation.
 */
public interface DocumentIndexer {

    /** Saves the text under the document id. Indexing the same document again replaces the entry. */
    void index(Long documentId, String originalFilename, String content);
}
