package com.paperless.rest.service;

/**
 * Stores and removes the binary files of documents.
 * The business layer only knows this interface, the object storage is a detail of the implementation.
 */
public interface DocumentStorage {

    /** Stores the content under the given key, an existing object with the same key is replaced. */
    void store(String key, byte[] content, String contentType);

    /** Deletes the object. A key that does not exist is not an error. */
    void delete(String key);
}
