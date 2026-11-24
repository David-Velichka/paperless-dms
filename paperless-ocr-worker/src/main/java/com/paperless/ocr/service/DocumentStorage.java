package com.paperless.ocr.service;

import java.nio.file.Path;

/**
 * Reads the binary files of documents from the object storage.
 * The worker only reads, it never writes or deletes objects.
 */
public interface DocumentStorage {

    /**
     * Downloads the object into the target directory and returns the path of the file.
     * A key that does not exist throws DocumentNotFoundInStorageException.
     */
    Path download(String key, Path targetDir);
}
