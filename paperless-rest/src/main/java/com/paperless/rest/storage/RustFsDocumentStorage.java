package com.paperless.rest.storage;

import com.paperless.rest.config.RustFsProperties;
import com.paperless.rest.exception.StorageException;
import com.paperless.rest.service.DocumentStorage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Stores document files in the RustFS bucket through the S3 API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RustFsDocumentStorage implements DocumentStorage {

    private final S3Client s3Client;
    private final RustFsProperties properties;

    // Creates the bucket on the first start, so no manual setup is needed
    @PostConstruct
    void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build());
            log.info("Storage bucket exists bucket={}", properties.bucket());
        } catch (NoSuchBucketException ex) {
            createBucket();
        } catch (S3Exception ex) {
            // A HEAD response has no body, so a missing bucket can also arrive as a plain 404
            if (ex.statusCode() != 404) {
                throw new StorageException("Storage bucket could not be checked", ex);
            }
            createBucket();
        } catch (SdkException ex) {
            throw new StorageException("Storage bucket could not be checked", ex);
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket()).build());
            log.info("Storage bucket created bucket={}", properties.bucket());
        } catch (SdkException ex) {
            throw new StorageException("Storage bucket could not be created", ex);
        }
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Document file stored key={} sizeBytes={}", key, content.length);
        } catch (SdkException ex) {
            log.error("Document file could not be stored key={}", key, ex);
            throw new StorageException("Document file could not be stored", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            // S3 answers 204 for a missing key, so this is idempotent
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
            log.info("Document file deleted key={}", key);
        } catch (SdkException ex) {
            log.error("Document file could not be deleted key={}", key, ex);
            throw new StorageException("Document file could not be deleted", ex);
        }
    }
}
