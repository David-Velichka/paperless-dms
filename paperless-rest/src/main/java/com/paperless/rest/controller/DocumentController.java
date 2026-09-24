package com.paperless.rest.controller;

import com.paperless.rest.dto.DocumentDto;
import com.paperless.rest.mapper.DocumentMapper;
import com.paperless.rest.service.DocumentService;
import com.paperless.rest.service.model.DocumentModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        log.info("REST request to get all documents");
        List<DocumentModel> models = documentService.findAll();
        return ResponseEntity.ok(documentMapper.toDtoList(models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id) {
        log.info("REST request to get document: {}", id);
        DocumentModel model = documentService.findById(id);
        return ResponseEntity.ok(documentMapper.toDto(model));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentDto>> searchDocuments(@RequestParam("query") String query) {
        log.info("REST request to search documents with query: {}", query);
        List<DocumentModel> results = documentService.search(query);
        return ResponseEntity.ok(documentMapper.toDtoList(results));
    }

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody DocumentDto dto) {
        log.info("REST request to create document metadata: {}", dto.getTitle());
        DocumentModel model = documentMapper.toModel(dto);
        DocumentModel saved = documentService.save(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentMapper.toDto(saved));
    }

    // Multipart post
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Document upload rejected: provided file is empty or missing");
            return ResponseEntity.badRequest().build();
        }

        String documentTitle = (title != null && !title.isBlank())
                ? title
                : (file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");

        log.info("REST request to upload document: {} (filename: {})", documentTitle, file.getOriginalFilename());

        DocumentModel model = DocumentModel.builder()
                .title(documentTitle)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        DocumentModel saved = documentService.uploadDocument(model, file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("REST request to delete document: {}", id);
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
