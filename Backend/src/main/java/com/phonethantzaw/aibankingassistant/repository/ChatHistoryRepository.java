package com.phonethantzaw.aibankingassistant.repository;

import com.phonethantzaw.aibankingassistant.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    List<ChatHistory> findBySessionIdAndUserIdOrderByCreatedAtDesc(String sessionId, String userId);
    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}
