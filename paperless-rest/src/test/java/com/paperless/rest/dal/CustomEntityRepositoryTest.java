package com.paperless.rest.dal;

import com.paperless.rest.dal.entity.CustomEntity;
import com.paperless.rest.dal.repository.CustomEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomEntityRepositoryTest {

    @Autowired
    private CustomEntityRepository customEntityRepository;

    // GIVEN eine neue benutzerdefinierte Entität mit Namen und Dokumenten-ID
    // WHEN die Entität im Repository gespeichert wird
    // THEN wird sie mit generierter ID und Erstellungszeitstempel persistiert
    @Test
    void saveAndFindById() {
        CustomEntity entity = CustomEntity.builder()
                .name("Invoice Tag")
                .description("Tax-related invoice category")
                .documentId(101L)
                .build();

        CustomEntity saved = customEntityRepository.save(entity);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Invoice Tag", saved.getName());

        Optional<CustomEntity> found = customEntityRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(101L, found.get().getDocumentId());
    }

    // GIVEN mehrere benutzerdefinierte Entitäten mit verschiedenen Dokumenten-IDs
    // WHEN nach einer bestimmten Dokumenten-ID gesucht wird
    // THEN werden nur die zugehörigen Entitäten zurückgegeben
    @Test
    void findByDocumentId() {
        CustomEntity entity1 = CustomEntity.builder()
                .name("Tag A")
                .documentId(201L)
                .build();
        CustomEntity entity2 = CustomEntity.builder()
                .name("Tag B")
                .documentId(201L)
                .build();
        CustomEntity entity3 = CustomEntity.builder()
                .name("Tag C")
                .documentId(999L)
                .build();

        customEntityRepository.save(entity1);
        customEntityRepository.save(entity2);
        customEntityRepository.save(entity3);

        List<CustomEntity> results = customEntityRepository.findByDocumentId(201L);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Tag A")));
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Tag B")));
    }

    // GIVEN eine gespeicherte benutzerdefinierte Entität
    // WHEN deleteById aufgerufen wird
    // THEN ist die Entität nicht mehr im Repository vorhanden
    @Test
    void deleteCustomEntity() {
        CustomEntity entity = CustomEntity.builder()
                .name("Temporary Category")
                .documentId(301L)
                .build();

        CustomEntity saved = customEntityRepository.save(entity);
        Long id = saved.getId();

        assertTrue(customEntityRepository.existsById(id));
        customEntityRepository.deleteById(id);
        assertFalse(customEntityRepository.existsById(id));
    }
}
