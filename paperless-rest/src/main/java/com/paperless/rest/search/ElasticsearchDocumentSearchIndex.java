package com.paperless.rest.search;

import com.paperless.rest.exception.SearchException;
import com.paperless.rest.service.DocumentSearchIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Searches the OCR text and the file name in Elasticsearch.
 * The index is created by the worker with the first indexed document.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchDocumentSearchIndex implements DocumentSearchIndex {

    private static final int MAX_RESULTS = 100;

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Long> search(String query) {
        // AUTO fuzziness allows 1 typo in words with 3 to 5 letters and 2 typos in longer words
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .fields("content", "originalFilename")
                        .query(query)
                        .fuzziness("AUTO")))
                .withMaxResults(MAX_RESULTS)
                .build();
        try {
            SearchHits<IndexedDocument> hits = elasticsearchOperations.search(nativeQuery, IndexedDocument.class);
            List<Long> ids = hits.getSearchHits().stream()
                    .map(hit -> Long.valueOf(hit.getId()))
                    .toList();
            log.info("Search index queried hits={}", ids.size());
            return ids;
        } catch (NoSuchIndexException ex) {
            // Nothing was indexed yet
            log.info("Search index does not exist yet, no hits");
            return List.of();
        } catch (DataAccessException ex) {
            log.error("Search index could not be queried", ex);
            throw new SearchException("Search index could not be queried", ex);
        }
    }

    @Override
    public void delete(Long documentId) {
        try {
            elasticsearchOperations.delete(String.valueOf(documentId), IndexedDocument.class);
            log.info("Search index entry deleted documentId={}", documentId);
        } catch (NoSuchIndexException ex) {
            log.info("Search index does not exist yet, nothing to delete documentId={}", documentId);
        } catch (DataAccessException ex) {
            throw new SearchException("Search index entry could not be deleted", ex);
        }
    }
}
