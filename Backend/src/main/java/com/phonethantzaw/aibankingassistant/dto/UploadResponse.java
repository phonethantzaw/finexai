package com.phonethantzaw.aibankingassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private Long documentId;
    private String filename;
    private String accountType;
    private Integer chunkCount;
    private Long fileSize;
    private LocalDateTime uploadTime;
    private String fileUrl;
    private String message;
}
