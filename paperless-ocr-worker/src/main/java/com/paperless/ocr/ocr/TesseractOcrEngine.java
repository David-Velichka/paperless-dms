package com.paperless.ocr.ocr;

import com.paperless.ocr.config.OcrProperties;
import com.paperless.ocr.exception.OcrException;
import com.paperless.ocr.service.OcrEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Recognizes the text of a PDF: Ghostscript renders the pages, Tesseract (Tess4J) reads each page image.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TesseractOcrEngine implements OcrEngine {

    private final PdfToImageConverter pdfToImageConverter;
    private final OcrProperties properties;

    @Override
    public String extractText(Path pdf) {
        // The page images are written next to the PDF, the caller removes the whole directory afterwards
        List<Path> pages = pdfToImageConverter.convert(pdf, pdf.getParent());

        // A Tesseract object is not thread safe, so every call creates its own
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(properties.tessdataPath());
        tesseract.setLanguage(properties.language());

        List<String> pageTexts = new ArrayList<>();
        try {
            for (Path page : pages) {
                pageTexts.add(tesseract.doOCR(page.toFile()).strip());
            }
        } catch (TesseractException ex) {
            throw new OcrException("Tesseract could not read a page image", ex);
        }

        String text = String.join("\n\n", pageTexts).strip();
        log.info("OCR finished pages={} textLength={}", pages.size(), text.length());
        return text;
    }
}
