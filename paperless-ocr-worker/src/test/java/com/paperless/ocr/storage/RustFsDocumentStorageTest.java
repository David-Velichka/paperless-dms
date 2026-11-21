package com.paperless.ocr.storage;

import com.paperless.ocr.config.RustFsProperties;
import com.paperless.ocr.exception.DocumentNotFoundInStorageException;
import com.paperless.ocr.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RustFsDocumentStorageTest {

    private static final RustFsProperties PROPERTIES =
            new RustFsProperties("http://rustfs:9000", "access", "secret", "test-bucket");

    @Mock
    private S3Client s3Client;

    @TempDir
    private Path tempDir;

    private RustFsDocumentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new RustFsDocumentStorage(s3Client, PROPERTIES);
    }

    // GIVEN ein Key eines gespeicherten Objekts und ein Zielverzeichnis
    // WHEN download aufgerufen wird
    // THEN wird das Objekt aus dem Bucket in das Zielverzeichnis geladen
    @Test
    void download_GetsObjectIntoTargetDirectory() {
        Path result = storage.download("abc.pdf", tempDir);

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture(), any(Path.class));
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("abc.pdf", captor.getValue().key());
        assertEquals(tempDir, result.getParent());
    }

    // GIVEN der Key existiert nicht im Bucket
    // WHEN download aufgerufen wird
    // THEN wird eine DocumentNotFoundInStorageException geworfen
    @Test
    void download_WhenKeyMissing_ThrowsNotFoundException() {
        doThrow(NoSuchKeyException.builder().message("missing").build())
                .when(s3Client).getObject(any(GetObjectRequest.class), any(Path.class));

        assertThrows(DocumentNotFoundInStorageException.class, () -> storage.download("abc.pdf", tempDir));
    }

    // GIVEN RustFS ist nicht erreichbar
    // WHEN download aufgerufen wird
    // THEN wird der SDK-Fehler in eine StorageException umgewandelt
    @Test
    void download_WhenSdkFails_ThrowsStorageException() {
        doThrow(SdkClientException.create("Connection refused"))
                .when(s3Client).getObject(any(GetObjectRequest.class), any(Path.class));

        StorageException ex = assertThrows(StorageException.class, () -> storage.download("abc.pdf", tempDir));
        assertInstanceOf(SdkClientException.class, ex.getCause());
    }
}
