package com.paperless.ocr.ocr;

import com.paperless.ocr.config.OcrProperties;
import com.paperless.ocr.exception.OcrException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Runs without Ghostscript and Tesseract, the page conversion is mocked.
 */
@ExtendWith(MockitoExtension.class)
class TesseractOcrEngineMockedTest {

    private static final Path PDF = Path.of("dir", "document.pdf");

    @Mock
    private PdfToImageConverter pdfToImageConverter;

    private TesseractOcrEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TesseractOcrEngine(pdfToImageConverter, new OcrProperties("tessdata", "eng"));
    }

    // GIVEN Ghostscript liefert keine Seitenbilder
    // WHEN extractText aufgerufen wird
    // THEN ist der erkannte Text leer
    @Test
    void extractText_WithoutPages_ReturnsEmptyText() {
        when(pdfToImageConverter.convert(PDF, PDF.getParent())).thenReturn(List.of());

        assertEquals("", engine.extractText(PDF));
    }

    // GIVEN Ghostscript kann das PDF nicht umwandeln
    // WHEN extractText aufgerufen wird
    // THEN wird die OcrException weitergegeben
    @Test
    void extractText_WhenConversionFails_PropagatesOcrException() {
        when(pdfToImageConverter.convert(PDF, PDF.getParent())).thenThrow(new OcrException("failed"));

        assertThrows(OcrException.class, () -> engine.extractText(PDF));
    }
}
