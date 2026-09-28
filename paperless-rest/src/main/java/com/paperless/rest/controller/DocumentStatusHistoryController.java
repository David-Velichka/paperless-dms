package com.paperless.rest.controller;

import com.paperless.rest.dto.DocumentStatusChangeRequestDto;
import com.paperless.rest.dto.DocumentStatusHistoryDto;
import com.paperless.rest.mapper.DocumentStatusHistoryMapper;
import com.paperless.rest.service.DocumentStatusHistoryService;
import com.paperless.rest.service.model.DocumentStatusHistoryModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusHistoryController {

    private final DocumentStatusHistoryService documentStatusHistoryService;
    private final DocumentStatusHistoryMapper documentStatusHistoryMapper;

    @GetMapping("/api/documents/{documentId}/history")
    public ResponseEntity<List<DocumentStatusHistoryDto>> getHistoryForDocument(@PathVariable Long documentId) {
        log.info("REST request to get status history for document: {}", documentId);
        List<DocumentStatusHistoryModel> models = documentStatusHistoryService.getHistoryByDocumentId(documentId);
        return ResponseEntity.ok(documentStatusHistoryMapper.toDtoList(models));
    }

    @PostMapping("/api/documents/{documentId}/status")
    public ResponseEntity<DocumentStatusHistoryDto> changeDocumentStatus(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentStatusChangeRequestDto request) {
        log.info("REST request to change status of document {} to {} by {}",
                documentId, request.getStatus(), request.getChangedBy());
        DocumentStatusHistoryModel result = documentStatusHistoryService.changeStatus(
                documentId, request.getStatus(), request.getChangedBy(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentStatusHistoryMapper.toDto(result));
    }

    @GetMapping("/api/documents/status-history/{id}")
    public ResponseEntity<DocumentStatusHistoryDto> getHistoryById(@PathVariable Long id) {
        log.info("REST request to get document status history entry: {}", id);
        DocumentStatusHistoryModel model = documentStatusHistoryService.findById(id);
        return ResponseEntity.ok(documentStatusHistoryMapper.toDto(model));
    }

}
