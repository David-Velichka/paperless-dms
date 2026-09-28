package com.paperless.rest.dal;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import com.paperless.rest.dal.repository.DocumentStatusHistoryRepository;
import com.paperless.rest.service.model.DocumentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DocumentStatusHistoryRepositoryTest {

    @Autowired
    private DocumentStatusHistoryRepository documentStatusHistoryRepository;

    @Autowired
    private DocumentRepository documentRepository;

    // GIVEN ein persistierter Statusverlaufseintrag
    // WHEN findById aufgerufen wird
    // THEN wird der Eintrag mit korrekter Dokumenten-ID und Status gefunden
    @Test
    void saveAndFindById() {
        DocumentStatusHistoryEntity entity = DocumentStatusHistoryEntity.builder()
                .documentId(101L)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .changedBy("system")
                .comment("Initial creation")
                .build();

        DocumentStatusHistoryEntity saved = documentStatusHistoryRepository.save(entity);

        assertNotNull(saved.getId());
        assertNotNull(saved.getTimestamp());
        assertEquals(DocumentStatus.RECEIVED, saved.getStatus());
        assertEquals("system", saved.getChangedBy());

        Optional<DocumentStatusHistoryEntity> found = documentStatusHistoryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(101L, found.get().getDocumentId());
    }

    // GIVEN mehrere Statuseinträge für dieselbe Dokumenten-ID mit unterschiedlichen Zeitstempeln
    // WHEN findByDocumentIdOrderByTimestampDesc aufgerufen wird
    // THEN werden die Einträge absteigend nach Zeitstempel sortiert zurückgegeben
    @Test
    void findByDocumentIdOrderByTimestampDesc() {
        OffsetDateTime now = OffsetDateTime.now();

        DocumentStatusHistoryEntity entry1 = DocumentStatusHistoryEntity.builder()
                .documentId(201L)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .changedBy("user1")
                .timestamp(now.minusHours(2))
                .comment("Upload")
                .build();

        DocumentStatusHistoryEntity entry2 = DocumentStatusHistoryEntity.builder()
                .documentId(201L)
                .status(DocumentStatus.PROCESSING)
                .previousStatus(DocumentStatus.RECEIVED)
                .changedBy("ocr-worker")
                .timestamp(now.minusHours(1))
                .comment("Processing")
                .build();

        DocumentStatusHistoryEntity otherDocEntry = DocumentStatusHistoryEntity.builder()
                .documentId(999L)
                .status(DocumentStatus.RECEIVED)
                .previousStatus(null)
                .changedBy("user2")
                .timestamp(now)
                .build();

        documentStatusHistoryRepository.save(entry1);
        documentStatusHistoryRepository.save(entry2);
        documentStatusHistoryRepository.save(otherDocEntry);

        List<DocumentStatusHistoryEntity> results = documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(201L);

        assertEquals(2, results.size());
        assertEquals(DocumentStatus.PROCESSING, results.get(0).getStatus());
        assertEquals(DocumentStatus.RECEIVED, results.get(1).getStatus());
    }

    // GIVEN gespeicherte Statuseinträge für eine Dokumenten-ID
    // WHEN deleteByDocumentId aufgerufen wird
    // THEN sind keine Einträge für dieses Dokument mehr vorhanden
    @Test
    void deleteByDocumentId() {
        DocumentStatusHistoryEntity entry = DocumentStatusHistoryEntity.builder()
                .documentId(301L)
                .status(DocumentStatus.RECEIVED)
                .changedBy("system")
                .build();

        documentStatusHistoryRepository.save(entry);
        assertFalse(documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(301L).isEmpty());

        documentStatusHistoryRepository.deleteByDocumentId(301L);
        assertTrue(documentStatusHistoryRepository.findByDocumentIdOrderByTimestampDesc(301L).isEmpty());
    }

    // GIVEN ein neues Dokument ohne expliziten Status
    // WHEN das Dokument persistiert wird
    // THEN wird der Standardstatus RECEIVED über PrePersist gesetzt
    @Test
    void documentEntity_DefaultStatusIsReceived() {
        DocumentEntity document = DocumentEntity.builder()
                .title("Status Test Document")
                .build();

        DocumentEntity saved = documentRepository.save(document);

        assertNotNull(saved.getId());
        assertEquals(DocumentStatus.RECEIVED, saved.getCurrentStatus());
    }
}
