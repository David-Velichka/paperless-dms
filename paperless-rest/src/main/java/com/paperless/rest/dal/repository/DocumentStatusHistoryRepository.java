package com.paperless.rest.dal.repository;

import com.paperless.rest.dal.entity.DocumentStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentStatusHistoryRepository extends JpaRepository<DocumentStatusHistoryEntity, Long> {
    List<DocumentStatusHistoryEntity> findByDocumentIdOrderByTimestampDesc(Long documentId);
    void deleteByDocumentId(Long documentId);
}
