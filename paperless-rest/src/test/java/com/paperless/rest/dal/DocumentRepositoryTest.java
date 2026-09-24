package com.paperless.rest.dal;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    // GIVEN ein neues Dokument mit Titel und Dateinamen
    // WHEN das Dokument im Repository gespeichert und anhand des Titels gesucht wird
    // THEN wird die Entität mit generierter ID persistiert und erfolgreich gefunden
    @Test
    void saveAndFindDocument() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Accounting Report 2026")
                .originalFilename("accounting.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .build();

        DocumentEntity saved = documentRepository.save(entity);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getModifiedAt());

        List<DocumentEntity> found = documentRepository.findByTitleContainingIgnoreCase("Accounting");
        assertFalse(found.isEmpty());
        assertEquals("Accounting Report 2026", found.get(0).getTitle());
    }

    // GIVEN ein persistiertes Dokument im Repository
    // WHEN nach einem nicht existierenden Titelbegriff gesucht wird
    // THEN gibt die Suche eine leere Liste zurück
    @Test
    void findByTitleContainingIgnoreCase_NoMatchReturnsEmpty() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Annual Review 2026")
                .originalFilename("review.pdf")
                .build();
        documentRepository.save(entity);

        List<DocumentEntity> results = documentRepository.findByTitleContainingIgnoreCase("NonExistentTermXYZ");
        assertTrue(results.isEmpty());
    }

    // GIVEN ein gespeichertes Dokument im Repository
    // WHEN nach dem Primärschlüssel gesucht wird
    // THEN wird das entsprechende Dokument zurückgegeben
    @Test
    void findById() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Tax Assessment")
                .originalFilename("tax.pdf")
                .build();
        DocumentEntity saved = documentRepository.save(entity);

        Optional<DocumentEntity> found = documentRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Tax Assessment", found.get().getTitle());
    }

    // GIVEN mehrere gespeicherte Dokumente in der Datenbank
    // WHEN findAll aufgerufen wird
    // THEN werden alle vorhandenen Dokumente zurückgegeben
    @Test
    void findAllDocuments() {
        DocumentEntity doc1 = DocumentEntity.builder().title("Doc 1").build();
        DocumentEntity doc2 = DocumentEntity.builder().title("Doc 2").build();
        documentRepository.save(doc1);
        documentRepository.save(doc2);

        List<DocumentEntity> all = documentRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    // GIVEN ein gespeichertes Dokument in der Datenbank
    // WHEN der Titel aktualisiert und gespeichert wird
    // THEN werden die Änderungen persistiert und das Modifizierungsdatum gesetzt
    @Test
    void updateDocument() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Draft Contract")
                .originalFilename("contract.pdf")
                .build();
        DocumentEntity saved = documentRepository.saveAndFlush(entity);

        saved.setTitle("Final Contract");
        DocumentEntity updated = documentRepository.saveAndFlush(saved);

        assertEquals("Final Contract", updated.getTitle());
        assertNotNull(updated.getModifiedAt());
    }

    // GIVEN ein gespeichertes Dokument in der Datenbank
    // WHEN deleteById aufgerufen wird
    // THEN existiert das Dokument anschließend nicht mehr im Repository
    @Test
    void deleteDocument() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Temp Document")
                .build();
        DocumentEntity saved = documentRepository.save(entity);
        Long id = saved.getId();

        assertTrue(documentRepository.existsById(id));
        documentRepository.deleteById(id);
        assertFalse(documentRepository.existsById(id));
    }
}
