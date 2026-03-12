package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.config.R2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client s3Client;
    private final R2Config r2Config;

    /**
     * Uploads a file to Cloudflare R2 under:
     *   documents/{userId}/{uuid}/{originalFilename}
     *
     * @return the full public URL — this is the only file reference stored in the DB.
     */
    public String upload(MultipartFile file, String userId) throws IOException {
        String key = "documents/%s/%s/%s".formatted(
                userId,
                UUID.randomUUID(),
                file.getOriginalFilename()
        );

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(r2Config.getBucketName())
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String publicUrl = r2Config.getPublicUrl() + "/" + key;
        log.info("Uploaded file to R2: {}", publicUrl);
        return publicUrl;
    }

    /**
     * Deletes a file from R2 using its stored public URL.
     *
     * @param fileUrl the full public URL as stored in the documents table.
     */
    public void delete(String fileUrl) {
        String key = fileUrl.replace(r2Config.getPublicUrl() + "/", "");
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(key)
                .build());
        log.info("Deleted file from R2: key={}", key);
    }
}

