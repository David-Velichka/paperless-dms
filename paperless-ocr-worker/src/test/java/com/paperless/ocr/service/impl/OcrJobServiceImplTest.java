package com.paperless.ocr.service.impl;

import com.paperless.ocr.exception.DocumentNotFoundInStorageException;
import com.paperless.ocr.exception.OcrException;
import com.paperless.ocr.messaging.OcrMessage;
import com.paperless.ocr.service.DocumentStorage;
import com.paperless.ocr.service.OcrEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrJobServiceImplTest {

    private static final OcrMessage MESSAGE =
            new OcrMessage(1L, "invoice.pdf", "application/pdf", OffsetDateTime.now(), "abc.pdf");

    @Mock
    private DocumentStorage documentStorage;

    @Mock
    private OcrEngine ocrEngine;

    @InjectMocks
    private OcrJobServiceImpl service;

    // GIVEN eine OCR-Nachricht mit Storage-Key
    // WHEN process aufgerufen wird
    // THEN wird zuerst das PDF geladen und danach der Text erkannt
    @Test
    void process_DownloadsPdfThenExtractsText() {
        Path pdf = Path.of("document.pdf");
        when(documentStorage.download(any(), any())).thenReturn(pdf);
        when(ocrEngine.extractText(pdf)).thenReturn("Hello");

        assertDoesNotThrow(() -> service.process(MESSAGE));

        InOrder order = inOrder(documentStorage, ocrEngine);
        order.verify(documentStorage).download(any(), any());
        order.verify(ocrEngine).extractText(pdf);
    }

    // GIVEN die Texterkennung liefert einen leeren Text
    // WHEN process aufgerufen wird
    // THEN ist das ein gültiges Ergebnis und es wird keine Exception geworfen
    @Test
    void process_WithEmptyText_IsValidResult() {
        Path pdf = Path.of("document.pdf");
        when(documentStorage.download(any(), any())).thenReturn(pdf);
        when(ocrEngine.extractText(pdf)).thenReturn("");

        assertDoesNotThrow(() -> service.process(MESSAGE));
    }

    // GIVEN die Texterkennung schlägt fehl
    // WHEN process aufgerufen wird
    // THEN wird die Exception weitergegeben und das Temp-Verzeichnis trotzdem gelöscht
    @Test
    void process_WhenOcrFails_PropagatesAndDeletesTempDir() {
        Path pdf = Path.of("document.pdf");
        when(documentStorage.download(any(), any())).thenReturn(pdf);
        when(ocrEngine.extractText(pdf)).thenThrow(new OcrException("failed"));

        assertThrows(OcrException.class, () -> service.process(MESSAGE));

        ArgumentCaptor<Path> dirCaptor = ArgumentCaptor.forClass(Path.class);
        verify(documentStorage).download(any(), dirCaptor.capture());
        assertFalse(Files.exists(dirCaptor.getValue()));
    }

    // GIVEN das PDF fehlt im Storage
    // WHEN process aufgerufen wird
    // THEN wird die Exception weitergegeben, die Texterkennung nicht gestartet und das Temp-Verzeichnis gelöscht
    @Test
    void process_WhenFileMissing_PropagatesAndSkipsOcr() {
        when(documentStorage.download(any(), any()))
                .thenThrow(new DocumentNotFoundInStorageException("missing", null));

        assertThrows(DocumentNotFoundInStorageException.class, () -> service.process(MESSAGE));

        verifyNoInteractions(ocrEngine);
        ArgumentCaptor<Path> dirCaptor = ArgumentCaptor.forClass(Path.class);
        verify(documentStorage).download(any(), dirCaptor.capture());
        assertFalse(Files.exists(dirCaptor.getValue()));
    }
}
