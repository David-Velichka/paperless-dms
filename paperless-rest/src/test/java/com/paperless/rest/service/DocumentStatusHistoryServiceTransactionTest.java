package com.paperless.rest.service;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.dal.persistence.DocumentStatusHistoryPersistenceService;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class DocumentStatusHistoryServiceTransactionTest {

    @Autowired
    private DocumentStatusHistoryService documentStatusHistoryService;

    @Autowired
    private DocumentPersistenceService documentPersistenceService;

    @MockitoBean
    private DocumentStatusHistoryPersistenceService documentStatusHistoryPersistenceService;

    // GIVEN ein gespeichertes Dokument und ein Fehler beim Speichern des Verlaufseintrags
    // WHEN der Status geändert wird
    // THEN bleibt der ursprüngliche Dokumentstatus erhalten
    @Test
    void changeStatus_WhenHistorySaveFails_RollsBackDocumentStatus() {
        DocumentModel savedDocument = documentPersistenceService.save(DocumentModel.builder()
                .title("Transaction rollback test")
                .currentStatus(DocumentStatus.RECEIVED)
                .build());
        Long documentId = savedDocument.getId();

        doThrow(new IllegalStateException("History save failed"))
                .when(documentStatusHistoryPersistenceService).save(any());

        try {
            assertThrows(IllegalStateException.class, () -> documentStatusHistoryService.changeStatus(
                    documentId, DocumentStatus.PROCESSING, "Test transaction rollback"));

            DocumentModel persistedDocument = documentPersistenceService.findById(documentId).orElseThrow();
            assertEquals(DocumentStatus.RECEIVED, persistedDocument.getCurrentStatus());
        } finally {
            documentPersistenceService.deleteById(documentId);
        }
    }
}
