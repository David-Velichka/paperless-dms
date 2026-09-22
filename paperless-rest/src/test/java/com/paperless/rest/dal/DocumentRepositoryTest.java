package com.paperless.rest.dal;

import com.paperless.rest.dal.entity.DocumentEntity;
import com.paperless.rest.dal.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    // GIVEN a new DocumentEntity with title and filename
    // WHEN save is invoked and queried by title
    // THEN entity is persisted with generated ID and retrievable via repository query
    @Test
    void saveAndFindDocument() {
        DocumentEntity entity = DocumentEntity.builder()
                .title("Accounting Report 2026")
                .originalFilename("accounting.pdf")
                .build();

        DocumentEntity saved = documentRepository.save(entity);
        assertNotNull(saved.getId());

        List<DocumentEntity> found = documentRepository.findByTitleContainingIgnoreCase("Accounting");
        assertFalse(found.isEmpty());
        assertEquals("Accounting Report 2026", found.get(0).getTitle());
    }
}
