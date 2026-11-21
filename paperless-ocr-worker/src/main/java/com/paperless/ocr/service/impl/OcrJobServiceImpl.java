package com.paperless.ocr.service.impl;

import com.paperless.ocr.exception.OcrException;
import com.paperless.ocr.messaging.OcrMessage;
import com.paperless.ocr.service.DocumentIndexer;
import com.paperless.ocr.service.DocumentStorage;
import com.paperless.ocr.service.OcrEngine;
import com.paperless.ocr.service.OcrJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrJobServiceImpl implements OcrJobService {

    private final DocumentStorage documentStorage;
    private final OcrEngine ocrEngine;
    private final DocumentIndexer documentIndexer;

    @Override
    public void process(OcrMessage message) {
        Path tempDir = createTempDir();
        try {
            Path pdf = documentStorage.download(message.storageKey(), tempDir);
            // An empty text is a valid result (for example a blank page)
            String text = ocrEngine.extractText(pdf);
            // Indexing the same document again replaces the entry, so a redelivered job is harmless
            documentIndexer.index(message.documentId(), message.originalFilename(), text);
            log.info("OCR job processed documentId={} textLength={}", message.documentId(), text.length());
        } finally {
            deleteTempDir(tempDir);
        }
    }

    private Path createTempDir() {
        try {
            return Files.createTempDirectory("ocr-");
        } catch (IOException ex) {
            throw new OcrException("Temp directory could not be created", ex);
        }
    }

    private void deleteTempDir(Path tempDir) {
        try {
            FileSystemUtils.deleteRecursively(tempDir);
        } catch (IOException ex) {
            log.warn("Temp directory could not be deleted dir={}", tempDir, ex);
        }
    }
}
