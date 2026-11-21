package com.paperless.rest.service;

import java.util.List;

/**
 * Full-text search over the recognized text of documents.
 * The business layer only knows this interface, the search engine is a detail of the implementation.
 */
public interface DocumentSearchIndex {

    /** Returns the ids of the matching documents, best match first. Typos are tolerated. */
    List<Long> search(String query);

    /** Removes the entry of the document. A missing entry is not an error. */
    void delete(Long documentId);
}
