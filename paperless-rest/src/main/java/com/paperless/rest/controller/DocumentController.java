package com.paperless.rest.controller;

import com.paperless.rest.dto.DocumentCreateDto;
import com.paperless.rest.dto.DocumentResponseDto;
import com.paperless.rest.dto.DocumentSearchQueryDto;
import com.paperless.rest.dto.DocumentUpdateDto;
import com.paperless.rest.exception.InvalidDocumentException;
import com.paperless.rest.mapper.DocumentDtoMapper;
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
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentDtoMapper documentDtoMapper;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getAllDocuments() {
        log.info("REST request to get all documents");
        List<DocumentModel> models = documentService.findAll();
        return ResponseEntity.ok(documentDtoMapper.toResponseDtoList(models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocumentById(@PathVariable Long id) {
        log.info("REST request to get document: {}", id);
        DocumentModel model = documentService.findById(id);
        return ResponseEntity.ok(documentDtoMapper.toResponseDto(model));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponseDto>> searchDocuments(@Valid @ModelAttribute DocumentSearchQueryDto searchQuery) {
        log.info("REST request to search documents with query: {}", searchQuery.getQuery());
        List<DocumentModel> results = documentService.search(searchQuery.getQuery());
        return ResponseEntity.ok(documentDtoMapper.toResponseDtoList(results));
    }

    @PostMapping
    public ResponseEntity<DocumentResponseDto> createDocument(@Valid @RequestBody DocumentCreateDto dto) {
        log.info("REST request to create document metadata: {}", dto.getTitle());
        DocumentModel model = documentDtoMapper.toModel(dto);
        DocumentModel saved = documentService.save(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDtoMapper.toResponseDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> updateDocument(@PathVariable Long id, @Valid @RequestBody DocumentUpdateDto dto) {
        log.info("REST request to update document metadata: {}", id);
        DocumentModel changes = documentDtoMapper.toModel(dto);
        DocumentModel updated = documentService.update(id, changes);
        return ResponseEntity.ok(documentDtoMapper.toResponseDto(updated));
    }

    // Multipart post
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponseDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Document upload rejected: provided file is empty or missing");
            throw new InvalidDocumentException("Uploaded file must not be empty");
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
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDtoMapper.toResponseDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("REST request to delete document: {}", id);
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
