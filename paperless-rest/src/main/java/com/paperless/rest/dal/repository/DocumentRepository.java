package com.paperless.rest.dal.repository;

import com.paperless.rest.dal.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByTitleContainingIgnoreCase(String title);

    // Newest upload first, the id keeps the order stable when two uploads have the same time
    List<DocumentEntity> findAllByOrderByCreatedAtDescIdDesc();
}
