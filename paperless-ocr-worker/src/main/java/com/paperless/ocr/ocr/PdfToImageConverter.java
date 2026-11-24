package com.paperless.ocr.ocr;

import com.paperless.ocr.exception.OcrException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Converts the pages of a PDF into PNG images with the Ghostscript command line.
 */
@Component
public class PdfToImageConverter {

    private static final int TIMEOUT_SECONDS = 120;

    /** Writes one PNG per page (page-001.png, ...) into the target directory and returns them in page order. */
    public List<Path> convert(Path pdf, Path targetDir) {
        Path gsLog = targetDir.resolve("gs.log");
        ProcessBuilder processBuilder = new ProcessBuilder(
                "gs", "-dSAFER", "-dBATCH", "-dNOPAUSE", "-sDEVICE=png16m", "-r300",
                "-sOutputFile=" + targetDir.resolve("page-%03d.png"), pdf.toString())
                .redirectErrorStream(true)
                .redirectOutput(gsLog.toFile());
        Process process = null;
        try {
            process = processBuilder.start();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new OcrException("Ghostscript did not finish within " + TIMEOUT_SECONDS + " seconds");
            }
            if (process.exitValue() != 0) {
                String output = Files.readString(gsLog, StandardCharsets.UTF_8);
                throw new OcrException("Ghostscript failed exitCode=" + process.exitValue() + " output=" + output);
            }
            try (Stream<Path> files = Files.list(targetDir)) {
                return files
                        .filter(file -> file.getFileName().toString().matches("page-\\d+\\.png"))
                        .sorted()
                        .toList();
            }
        } catch (IOException ex) {
            throw new OcrException("Ghostscript could not be run", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OcrException("Ghostscript was interrupted", ex);
        } finally {
            // Kill a hanging or interrupted gs, so no Ghostscript process keeps running after the job
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
