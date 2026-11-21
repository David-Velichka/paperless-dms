package com.paperless.ocr.ocr;

import com.paperless.ocr.exception.OcrException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfToImageConverterTest {

    @TempDir
    private Path tempDir;

    // GIVEN eine Datei, die kein gültiges PDF ist
    // WHEN die Datei mit Ghostscript umgewandelt wird
    // THEN wird eine OcrException geworfen (auch wenn Ghostscript nicht installiert ist)
    @Test
    void convert_WithInvalidPdf_ThrowsOcrException() throws IOException {
        Path pdf = Files.writeString(tempDir.resolve("broken.pdf"), "no pdf content");

        assertThrows(OcrException.class, () -> new PdfToImageConverter().convert(pdf, tempDir));
    }
}
