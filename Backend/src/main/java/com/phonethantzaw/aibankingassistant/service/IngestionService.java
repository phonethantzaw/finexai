package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.model.DocumentRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IngestionService {

    DocumentRecord ingestDocument(MultipartFile file, String accountType, String userId) throws IOException;

    List<DocumentRecord> getDocumentsByUser(String userId);

    /**
     * Deletes a document from both Cloudflare R2 and the database.
     * Only deletes if the document belongs to the given userId (ownership enforced).
     */
    void deleteDocument(Long documentId, String userId);

}
