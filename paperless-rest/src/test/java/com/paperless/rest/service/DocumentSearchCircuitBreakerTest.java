package com.paperless.rest.service;

import com.paperless.rest.dal.persistence.DocumentPersistenceService;
import com.paperless.rest.exception.SearchException;
import com.paperless.rest.service.model.DocumentModel;
import com.paperless.rest.service.model.DocumentStatus;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class DocumentSearchCircuitBreakerTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentPersistenceService documentPersistenceService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private DocumentSearchIndex documentSearchIndex;

    // No RustFS in the test run: the real storage would connect to it at startup
    @MockitoBean
    private DocumentStorage documentStorage;

    private Long documentId;

    @BeforeEach
    void setUp() {
        // The breaker state survives between tests in the cached context
        circuitBreakerRegistry.circuitBreaker("search").reset();
        documentId = documentPersistenceService.save(DocumentModel.builder()
                .title("Breaker Invoice")
                .currentStatus(DocumentStatus.RECEIVED)
                .build()).getId();
        when(documentSearchIndex.search("Breaker"))
                .thenThrow(new SearchException("Search index could not be queried", new RuntimeException("down")));
    }

    @AfterEach
    void tearDown() {
        documentPersistenceService.deleteById(documentId);
    }

    // GIVEN Elasticsearch ist nicht erreichbar
    // WHEN eine Suche ausgeführt wird
    // THEN liefert der Fallback die Treffer der Titelsuche aus der Datenbank
    @Test
    void search_WhenIndexFails_ReturnsTitleMatches() {
        List<DocumentModel> result = documentService.search("Breaker");

        assertEquals(List.of(documentId), result.stream().map(DocumentModel::getId).toList());
    }

    // GIVEN Elasticsearch schlägt bei den ersten fünf Suchen fehl
    // WHEN danach weiter gesucht wird
    // THEN ist der Circuit Breaker offen und Elasticsearch wird nicht mehr aufgerufen
    @Test
    void search_AfterFailureThreshold_OpensBreakerAndSkipsIndex() {
        for (int i = 0; i < 5; i++) {
            documentService.search("Breaker");
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker("search").getState());

        List<DocumentModel> result = documentService.search("Breaker");

        assertEquals(List.of(documentId), result.stream().map(DocumentModel::getId).toList());
        verify(documentSearchIndex, times(5)).search("Breaker");
    }
}
