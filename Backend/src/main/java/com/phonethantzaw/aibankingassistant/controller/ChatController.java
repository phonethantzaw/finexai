package com.phonethantzaw.aibankingassistant.controller;

import com.phonethantzaw.aibankingassistant.dto.ChatRequest;
import com.phonethantzaw.aibankingassistant.dto.ChatResponse;
import com.phonethantzaw.aibankingassistant.model.ChatHistory;
import com.phonethantzaw.aibankingassistant.service.RagService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    @Autowired
    private RagService ragService;

    // Extract the Clerk userId from the verified JWT "sub" claim
    private String getUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        String userId = getUserId(authentication);
        log.info("Received chat request from session: {} (userId: {})", request.getSessionId(), userId);

        Map<String, Object> result = ragService.askQuestion(request.getQuestion(), request.getSessionId(), userId);

        ChatResponse response = ChatResponse.builder()
                .answer((String) result.get("answer"))
                .sources((List<Map<String, Object>>) result.get("sources"))
                .latencyMs((Long) result.get("latencyMs"))
                .sessionId((String) result.get("sessionId"))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatHistory>> getChatHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        log.info("Fetching chat history for session: {} (userId: {})", sessionId, userId);
        List<ChatHistory> history = ragService.getChatHistory(sessionId, userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getAllChatHistories(Authentication authentication) {
        String userId = getUserId(authentication);
        log.info("Fetching all chat histories for userId: {}", userId);
        List<ChatHistory> histories = ragService.getAllChatHistoriesByUser(userId);
        return ResponseEntity.ok(histories);
    }
}
