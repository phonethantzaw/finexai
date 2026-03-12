package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.model.DocumentRecord;
import com.phonethantzaw.aibankingassistant.repository.DocumentRepository;
import com.phonethantzaw.aibankingassistant.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class IngestionServiceImpl implements IngestionService {

    private final DocumentParserService documentParserService;
    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final R2StorageService r2StorageService;

    @Value("${app.ingestion.chunk-size:800}")
    private int chunkSize;

    @Value("${app.ingestion.chunk-overlap:200}")
    private int chunkOverlap;

    public IngestionServiceImpl(DocumentParserService documentParserService,
                                 VectorStore vectorStore,
                                 DocumentRepository documentRepository,
                                 R2StorageService r2StorageService) {
        this.documentParserService = documentParserService;
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.r2StorageService = r2StorageService;
    }

    @Override
    public DocumentRecord ingestDocument(MultipartFile file, String accountType, String userId) throws IOException {
        String filename = file.getOriginalFilename();
        log.info("Starting ingestion for file: {} (accountType: {}, userId: {})", filename, accountType, userId);

        // Step 1: Upload to R2 FIRST — no DB record is created if this fails
        String fileUrl = r2StorageService.upload(file, userId);

        try {
            // Step 2: Parse text content from the uploaded file
            String text = documentParserService.parseDocument(file);

            // Step 3: Chunk and embed into the vector store
            List<String> chunks = createOverlappingChunks(text, chunkSize, chunkOverlap);
            log.info("Created {} chunks from document", chunks.size());

            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("filename", filename);
                metadata.put("accountType", accountType);
                metadata.put("userId", userId);
                metadata.put("fileUrl", fileUrl);
                metadata.put("chunkIndex", i);
                metadata.put("totalChunks", chunks.size());
                documents.add(new Document(chunks.get(i), metadata));
            }

            vectorStore.add(documents);
            log.info("Stored {} chunks in vector store", documents.size());

            // Step 4: Save document metadata to DB — only after R2 + vector store succeed
            DocumentRecord record = DocumentRecord.builder()
                    .userId(userId)
                    .filename(filename)
                    .accountType(accountType)
                    .chunkCount(chunks.size())
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .build();

            return documentRepository.save(record);

        } catch (Exception e) {
            // Clean up the R2 object so we don't leave orphaned files
            log.error("Ingestion failed after R2 upload — cleaning up R2 object: {}", fileUrl, e);
            try {
                r2StorageService.delete(fileUrl);
            } catch (Exception deleteEx) {
                log.error("Failed to clean up R2 object after ingestion failure: {}", fileUrl, deleteEx);
            }
            throw e;
        }
    }

    @Override
    public List<DocumentRecord> getDocumentsByUser(String userId) {
        return documentRepository.findByUserId(userId);
    }

    @Override
    public void deleteDocument(Long documentId, String userId) {
        // Ownership-safe lookup — throws 404 if not found or not owned by this user
        DocumentRecord record = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id: " + documentId));

        // Delete from R2 first, then remove the DB record
        r2StorageService.delete(record.getFileUrl());
        documentRepository.delete(record);
        log.info("Deleted document {} (userId: {})", documentId, userId);
    }

    private List<String> createOverlappingChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            if (end < textLength) {
                int lastNewline = text.lastIndexOf('\n', end);
                if (lastNewline > start && lastNewline < end) {
                    end = lastNewline;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= textLength) break;

            start = end - overlap;
            if (start < 0) start = 0;
        }

        return chunks;
    }
}
