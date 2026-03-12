package com.phonethantzaw.aibankingassistant.repository;

import com.phonethantzaw.aibankingassistant.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentRecord, Long> {
    List<DocumentRecord> findByAccountType(String accountType);
    List<DocumentRecord> findByUserId(String userId);
    Optional<DocumentRecord> findByIdAndUserId(Long id, String userId);
}
