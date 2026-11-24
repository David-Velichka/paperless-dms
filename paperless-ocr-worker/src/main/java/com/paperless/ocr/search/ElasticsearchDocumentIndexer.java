package com.paperless.ocr.search;

import com.paperless.ocr.exception.IndexingException;
import com.paperless.ocr.service.DocumentIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

/**
 * Stores the recognized text in Elasticsearch.
 * Uses the template and no repository, because a repository would connect at startup and stop the worker
 * when Elasticsearch is not reachable. The index is created by the first save.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchDocumentIndexer implements DocumentIndexer {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(Long documentId, String originalFilename, String content) {
        try {
            // The document id is the index id, so saving the same document again replaces the entry
            elasticsearchOperations.save(new IndexedDocument(String.valueOf(documentId), originalFilename, content));
            log.info("Document indexed documentId={} textLength={}", documentId, content.length());
        } catch (DataAccessException ex) {
            log.error("Document could not be indexed documentId={}", documentId, ex);
            throw new IndexingException("Document " + documentId + " could not be indexed", ex);
        }
    }
}
