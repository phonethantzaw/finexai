package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.model.ChatHistory;
import com.phonethantzaw.aibankingassistant.repository.ChatHistoryRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${app.rag.top-k-results:5}")
    private int topK;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a helpful banking assistant. Answer the user's question based ONLY on the following context from their bank statements.
            
            Context:
            {context}
            
            Question: {question}
            
            Instructions:
            - Provide a clear, concise answer based on the context above
            - If you cannot find the answer in the context, say "I don't have enough information to answer that question based on your uploaded documents."
            - Include specific numbers, dates, and transaction details when relevant
            - Be accurate and do not hallucinate information not present in the context
            
            Answer:
            """;

    public RagServiceImpl(VectorStore vectorStore,
                           ChatModel chatModel,
                           ChatHistoryRepository chatHistoryRepository) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public Map<String, Object> askQuestion(String question, String sessionId, String userId) {
        long startTime = System.currentTimeMillis();
        log.info("Processing question for session {} (userId: {}): {}", sessionId, userId, question);

        // Filter similarity search to only the authenticated user's documents
        FilterExpressionBuilder fb = new FilterExpressionBuilder();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .filterExpression(fb.eq("userId", userId).build())
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} similar documents for userId: {}", similarDocuments.size(), userId);

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        Map<String, Object> promptVariables = new HashMap<>();
        promptVariables.put("context", context);
        promptVariables.put("question", question);

        PromptTemplate promptTemplate = new PromptTemplate(SYSTEM_PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(promptVariables);

        String answer = chatModel.call(prompt).getResult().getOutput().getText();

        long latencyMs = System.currentTimeMillis() - startTime;
        log.info("Generated answer in {}ms", latencyMs);

        List<Map<String, Object>> sources = similarDocuments.stream()
                .map(doc -> {
                    Map<String, Object> source = new HashMap<>();
                    source.put("content", doc.getText().substring(0, Math.min(200, doc.getText().length())) + "...");
                    source.put("metadata", doc.getMetadata());
                    return source;
                })
                .toList();

        String sourcesJson = sources.stream()
                .map(s -> (String) ((Map<String, Object>) s.get("metadata")).get("filename"))
                .distinct()
                .collect(Collectors.joining(", "));

        ChatHistory chatHistory = ChatHistory.builder()
                .userId(userId)
                .sessionId(sessionId)
                .question(question)
                .answer(answer)
                .sources(sourcesJson)
                .latencyMs(latencyMs)
                .build();

        chatHistoryRepository.save(chatHistory);

        Map<String, Object> response = new HashMap<>();
        response.put("answer", answer);
        response.put("sources", sources);
        response.put("latencyMs", latencyMs);
        response.put("sessionId", sessionId);

        return response;
    }

    @Override
    public List<ChatHistory> getChatHistory(String sessionId, String userId) {
        return chatHistoryRepository.findBySessionIdAndUserIdOrderByCreatedAtDesc(sessionId, userId);
    }

    @Override
    public List<ChatHistory> getAllChatHistoriesByUser(String userId) {
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
