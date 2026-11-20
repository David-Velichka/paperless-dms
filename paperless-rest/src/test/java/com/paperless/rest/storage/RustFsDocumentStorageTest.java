package com.paperless.rest.storage;

import com.paperless.rest.config.RustFsProperties;
import com.paperless.rest.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RustFsDocumentStorageTest {

    private static final RustFsProperties PROPERTIES =
            new RustFsProperties("http://rustfs:9000", "access", "secret", "test-bucket");

    @Mock
    private S3Client s3Client;

    private RustFsDocumentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new RustFsDocumentStorage(s3Client, PROPERTIES);
    }

    // GIVEN ein Dateiinhalt, ein Key und ein Content-Type
    // WHEN store aufgerufen wird
    // THEN wird das Objekt mit Bucket, Key und Content-Type im S3-Client abgelegt
    @Test
    void store_PutsObjectIntoBucket() {
        storage.store("abc.pdf", "content".getBytes(), "application/pdf");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("abc.pdf", captor.getValue().key());
        assertEquals("application/pdf", captor.getValue().contentType());
    }

    // GIVEN RustFS ist nicht erreichbar
    // WHEN store aufgerufen wird
    // THEN wird der SDK-Fehler in eine StorageException umgewandelt
    @Test
    void store_WhenSdkFails_ThrowsStorageException() {
        doThrow(SdkClientException.create("Connection refused"))
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        StorageException ex = assertThrows(StorageException.class,
                () -> storage.store("abc.pdf", "content".getBytes(), "application/pdf"));
        assertInstanceOf(SdkClientException.class, ex.getCause());
    }

    // GIVEN ein Key eines gespeicherten Objekts
    // WHEN delete aufgerufen wird
    // THEN wird das Objekt aus dem Bucket gelöscht
    @Test
    void delete_RemovesObjectFromBucket() {
        storage.delete("abc.pdf");

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("abc.pdf", captor.getValue().key());
    }

    // GIVEN RustFS ist nicht erreichbar
    // WHEN delete aufgerufen wird
    // THEN wird der SDK-Fehler in eine StorageException umgewandelt
    @Test
    void delete_WhenSdkFails_ThrowsStorageException() {
        doThrow(SdkClientException.create("Connection refused"))
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertThrows(StorageException.class, () -> storage.delete("abc.pdf"));
    }

    // GIVEN der Bucket existiert bereits
    // WHEN die Anwendung startet (ensureBucketExists)
    // THEN wird kein Bucket angelegt
    @Test
    void ensureBucketExists_WhenBucketExists_DoesNotCreateIt() {
        storage.ensureBucketExists();

        verify(s3Client).headBucket(any(HeadBucketRequest.class));
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }

    // GIVEN der Bucket existiert nicht (NoSuchBucket)
    // WHEN die Anwendung startet
    // THEN wird der Bucket angelegt
    @Test
    void ensureBucketExists_WhenBucketMissing_CreatesIt() {
        doThrow(NoSuchBucketException.builder().message("missing").build())
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        storage.ensureBucketExists();

        ArgumentCaptor<CreateBucketRequest> captor = ArgumentCaptor.forClass(CreateBucketRequest.class);
        verify(s3Client).createBucket(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
    }

    // GIVEN RustFS antwortet auf HEAD mit einem allgemeinen 404
    // WHEN die Anwendung startet
    // THEN wird der Bucket ebenfalls angelegt
    @Test
    void ensureBucketExists_WhenPlain404_CreatesIt() {
        doThrow(S3Exception.builder().statusCode(404).message("not found").build())
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        storage.ensureBucketExists();

        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    // GIVEN RustFS lehnt den Zugriff ab (403)
    // WHEN die Anwendung startet
    // THEN wird kein Bucket angelegt und eine StorageException geworfen
    @Test
    void ensureBucketExists_WhenAccessDenied_ThrowsStorageException() {
        doThrow(S3Exception.builder().statusCode(403).message("forbidden").build())
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        assertThrows(StorageException.class, () -> storage.ensureBucketExists());
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }

    // GIVEN RustFS ist beim Start nicht erreichbar
    // WHEN die Anwendung startet
    // THEN wird eine StorageException geworfen
    @Test
    void ensureBucketExists_WhenUnreachable_ThrowsStorageException() {
        doThrow(SdkClientException.create("Connection refused"))
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        assertThrows(StorageException.class, () -> storage.ensureBucketExists());
    }
}
