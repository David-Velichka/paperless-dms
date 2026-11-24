package com.paperless.ocr.ocr;

import com.paperless.ocr.config.OcrProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Real run with Ghostscript and Tesseract. Skipped on machines where they are not installed (for example Windows),
 * runs in CI.
 */
class TesseractOcrEngineTest {

    private static final Path HELLO_WORLD_PDF = Path.of("..", "tests", "sample-documents", "HelloWorld.pdf");

    @TempDir
    private Path tempDir;

    private TesseractOcrEngine engine;

    @BeforeEach
    void setUp() {
        String tessdataPath = System.getenv("TESSDATA_PREFIX");
        assumeTrue(tessdataPath != null && Files.isDirectory(Path.of(tessdataPath)), "Tesseract language files not found");
        assumeTrue(isGhostscriptInstalled(), "Ghostscript is not installed");
        assumeTrue(Files.exists(HELLO_WORLD_PDF), "HelloWorld.pdf not found");

        engine = new TesseractOcrEngine(new PdfToImageConverter(), new OcrProperties(tessdataPath, "eng"));
    }

    // GIVEN eine einseitige PDF-Datei mit dem Text "Hello World"
    // WHEN der Text mit Ghostscript und Tesseract erkannt wird
    // THEN enthält der erkannte Text das Wort "Hello"
    @Test
    void extractText_FromHelloWorldPdf_ContainsHello() throws IOException {
        Path pdf = Files.copy(HELLO_WORLD_PDF, tempDir.resolve("document.pdf"));

        String text = engine.extractText(pdf);

        assertTrue(text.contains("Hello"), "recognized text was: " + text);
    }

    private static boolean isGhostscriptInstalled() {
        try {
            Process process = new ProcessBuilder("gs", "--version").redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (IOException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
