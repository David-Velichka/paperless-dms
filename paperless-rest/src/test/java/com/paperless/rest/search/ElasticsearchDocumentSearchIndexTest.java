package com.paperless.rest.search;

import com.paperless.rest.exception.SearchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchDocumentSearchIndexTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private SearchHits<IndexedDocument> searchHits;

    @InjectMocks
    private ElasticsearchDocumentSearchIndex searchIndex;

    // GIVEN Elasticsearch liefert zwei Treffer in Relevanz-Reihenfolge
    // WHEN search aufgerufen wird
    // THEN werden die Index-IDs als Dokument-IDs in derselben Reihenfolge zurückgegeben
    @Test
    void search_MapsHitIdsInRelevanceOrder() {
        SearchHit<IndexedDocument> first = hit("7");
        SearchHit<IndexedDocument> second = hit("3");
        when(searchHits.getSearchHits()).thenReturn(List.of(first, second));
        when(elasticsearchOperations.search(any(Query.class), eq(IndexedDocument.class))).thenReturn(searchHits);

        List<Long> ids = searchIndex.search("Hello");

        assertEquals(List.of(7L, 3L), ids);
    }

    // GIVEN der Index existiert noch nicht, weil noch nichts indexiert wurde
    // WHEN search aufgerufen wird
    // THEN wird eine leere Liste statt eines Fehlers zurückgegeben
    @Test
    void search_WhenIndexMissing_ReturnsEmptyList() {
        when(elasticsearchOperations.search(any(Query.class), eq(IndexedDocument.class)))
                .thenThrow(new NoSuchIndexException("paperless-documents"));

        List<Long> ids = searchIndex.search("Hello");

        assertTrue(ids.isEmpty());
    }

    // GIVEN Elasticsearch ist nicht erreichbar
    // WHEN search aufgerufen wird
    // THEN wird der Fehler in eine SearchException umgewandelt
    @Test
    void search_WhenElasticsearchFails_ThrowsSearchException() {
        when(elasticsearchOperations.search(any(Query.class), eq(IndexedDocument.class)))
                .thenThrow(new DataAccessResourceFailureException("Connection refused"));

        SearchException ex = assertThrows(SearchException.class, () -> searchIndex.search("Hello"));
        assertInstanceOf(DataAccessResourceFailureException.class, ex.getCause());
    }

    // GIVEN eine Dokument-ID
    // WHEN delete aufgerufen wird
    // THEN wird der Eintrag mit dieser ID aus dem Index gelöscht
    @Test
    void delete_DeletesEntryWithDocumentIdAsId() {
        searchIndex.delete(7L);

        verify(elasticsearchOperations).delete("7", IndexedDocument.class);
    }

    // GIVEN der Index existiert noch nicht
    // WHEN delete aufgerufen wird
    // THEN wird kein Fehler geworfen
    @Test
    void delete_WhenIndexMissing_DoesNotThrow() {
        when(elasticsearchOperations.delete("7", IndexedDocument.class))
                .thenThrow(new NoSuchIndexException("paperless-documents"));

        assertDoesNotThrow(() -> searchIndex.delete(7L));
    }

    // GIVEN Elasticsearch ist nicht erreichbar
    // WHEN delete aufgerufen wird
    // THEN wird der Fehler in eine SearchException umgewandelt
    @Test
    void delete_WhenElasticsearchFails_ThrowsSearchException() {
        when(elasticsearchOperations.delete("7", IndexedDocument.class))
                .thenThrow(new DataAccessResourceFailureException("Connection refused"));

        assertThrows(SearchException.class, () -> searchIndex.delete(7L));
    }

    @SuppressWarnings("unchecked")
    private SearchHit<IndexedDocument> hit(String id) {
        SearchHit<IndexedDocument> hit = mock(SearchHit.class);
        when(hit.getId()).thenReturn(id);
        return hit;
    }
}
