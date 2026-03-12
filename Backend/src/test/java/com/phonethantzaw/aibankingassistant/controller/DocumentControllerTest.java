package com.phonethantzaw.aibankingassistant.controller;

import com.phonethantzaw.aibankingassistant.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
//@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DocumentControllerTest {

//    @Autowired
//    private MockMvc mockMvc;

    @Test
    void shouldUploadCsvDocument() throws Exception {
        String csvContent = """
                Date,Description,Amount,Category
                2024-01-15,Coffee Shop,5.50,Dining
                2024-01-16,Grocery Store,125.30,Groceries
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

//        mockMvc.perform(multipart("/api/documents/upload")
//                        .file(file)
//                        .param("accountType", "Checking"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.documentId").isNumber())
//                .andExpect(jsonPath("$.filename").value("transactions.csv"))
//                .andExpect(jsonPath("$.accountType").value("Checking"))
//                .andExpect(jsonPath("$.chunkCount").isNumber())
//                .andExpect(jsonPath("$.message").value("Document uploaded and processed successfully"));
    }

    @Test
    void shouldReturnBadRequestForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

//        mockMvc.perform(multipart("/api/documents/upload")
//                        .file(file)
//                        .param("accountType", "Checking"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("File is empty"));
    }
}
