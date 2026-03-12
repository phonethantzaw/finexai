package com.phonethantzaw.aibankingassistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    private String question;

    @NotBlank(message = "Session ID cannot be empty")
    private String sessionId;
}
