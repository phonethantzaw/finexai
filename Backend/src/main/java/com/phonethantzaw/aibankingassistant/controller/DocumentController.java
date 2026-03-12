package com.phonethantzaw.aibankingassistant.controller;

import com.phonethantzaw.aibankingassistant.dto.UploadResponse;
import com.phonethantzaw.aibankingassistant.model.DocumentRecord;
import com.phonethantzaw.aibankingassistant.service.IngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    @Autowired
    private IngestionService ingestionService;

    // Extract the Clerk userId from the verified JWT "sub" claim
    private String getUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "accountType", required = false, defaultValue = "General") String accountType,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("File is empty")
                            .build());
        }

        try {
            String userId = getUserId(authentication);
            DocumentRecord record = ingestionService.ingestDocument(file, accountType, userId);

            UploadResponse response = UploadResponse.builder()
                    .documentId(record.getId())
                    .filename(record.getFilename())
                    .accountType(record.getAccountType())
                    .chunkCount(record.getChunkCount())
                    .fileSize(record.getFileSize())
                    .uploadTime(record.getUploadTime())
                    .fileUrl(record.getFileUrl())
                    .message("Document uploaded and processed successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error processing document upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UploadResponse.builder()
                            .message("Failed to process document: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDocuments(Authentication authentication) {
        try {
            String userId = getUserId(authentication);
            return ResponseEntity.ok(ingestionService.getDocumentsByUser(userId));
        } catch (Exception e) {
            log.error("Error fetching documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch documents: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            Authentication authentication) {
        String userId = getUserId(authentication);
        log.info("Deleting document {} for userId: {}", id, userId);
        ingestionService.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }
}
