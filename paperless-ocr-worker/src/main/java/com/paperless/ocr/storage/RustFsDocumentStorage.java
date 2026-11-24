package com.paperless.ocr.storage;

import com.paperless.ocr.config.RustFsProperties;
import com.paperless.ocr.exception.DocumentNotFoundInStorageException;
import com.paperless.ocr.exception.StorageException;
import com.paperless.ocr.service.DocumentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.file.Path;

/**
 * Reads document files from the RustFS bucket through the S3 API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RustFsDocumentStorage implements DocumentStorage {

    private final S3Client s3Client;
    private final RustFsProperties properties;

    @Override
    public Path download(String key, Path targetDir) {
        // Fixed file name: the key comes from a message and must not decide where the file is written
        Path target = targetDir.resolve("document.pdf");
        try {
            s3Client.getObject(GetObjectRequest.builder().bucket(properties.bucket()).key(key).build(), target);
            log.info("Document file downloaded key={}", key);
            return target;
        } catch (NoSuchKeyException ex) {
            throw new DocumentNotFoundInStorageException("Document file does not exist in the storage", ex);
        } catch (SdkException ex) {
            log.error("Document file could not be downloaded key={}", key, ex);
            throw new StorageException("Document file could not be downloaded", ex);
        }
    }
}
