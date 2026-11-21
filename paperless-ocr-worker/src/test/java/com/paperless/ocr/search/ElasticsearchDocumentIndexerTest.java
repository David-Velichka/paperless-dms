package com.paperless.ocr.search;

import com.paperless.ocr.exception.IndexingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchDocumentIndexerTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ElasticsearchDocumentIndexer indexer;

    // GIVEN eine Dokument-ID, ein Dateiname und ein erkannter Text
    // WHEN index aufgerufen wird
    // THEN wird ein Eintrag mit der Dokument-ID als Index-ID gespeichert
    @Test
    void index_SavesEntryWithDocumentIdAsId() {
        indexer.index(7L, "invoice.pdf", "Hello World");

        ArgumentCaptor<IndexedDocument> captor = ArgumentCaptor.forClass(IndexedDocument.class);
        verify(elasticsearchOperations).save(captor.capture());
        assertEquals("7", captor.getValue().id());
        assertEquals("invoice.pdf", captor.getValue().originalFilename());
        assertEquals("Hello World", captor.getValue().content());
    }

    // GIVEN Elasticsearch ist nicht erreichbar
    // WHEN index aufgerufen wird
    // THEN wird der Fehler in eine IndexingException umgewandelt
    @Test
    void index_WhenElasticsearchFails_ThrowsIndexingException() {
        when(elasticsearchOperations.save(org.mockito.ArgumentMatchers.any(IndexedDocument.class)))
                .thenThrow(new DataAccessResourceFailureException("Connection refused"));

        IndexingException ex = assertThrows(IndexingException.class,
                () -> indexer.index(7L, "invoice.pdf", "Hello World"));
        assertInstanceOf(DataAccessResourceFailureException.class, ex.getCause());
    }
}
