package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.model.ChatHistory;

import java.util.List;
import java.util.Map;

public interface RagService {
    /**
     * Process a user question using RAG, scoped to the authenticated user's documents.
     *
     * @param question  The user's question to be processed.
     * @param sessionId The unique identifier for the user's session.
     * @param userId    The authenticated user's ID (from Clerk JWT sub claim).
     * @return A map containing the answer, sources, latency in milliseconds, and session ID.
     */
    Map<String, Object> askQuestion(String question, String sessionId, String userId);

    /**
     * Retrieve the chat history for a given session, scoped to the authenticated user.
     *
     * @param sessionId The unique identifier for the user's session.
     * @param userId    The authenticated user's ID (from Clerk JWT sub claim).
     * @return A list of ChatHistory objects for this session belonging to the user.
     */
    List<ChatHistory> getChatHistory(String sessionId, String userId);

    /**
     * Retrieve all chat histories for the authenticated user.
     *
     * @param userId The authenticated user's ID (from Clerk JWT sub claim).
     * @return A list of all ChatHistory objects for this user.
     */
    List<ChatHistory> getAllChatHistoriesByUser(String userId);
}
