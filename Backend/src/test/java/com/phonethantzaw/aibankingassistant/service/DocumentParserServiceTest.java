package com.phonethantzaw.aibankingassistant.service;

import com.phonethantzaw.aibankingassistant.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DocumentParserServiceTest {

    @Autowired
    private DocumentParserService documentParserService;

    @Test
    void shouldParseCsvFile() throws IOException {
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

        String result = documentParserService.parseDocument(file);

        assertThat(result).isNotNull();
        assertThat(result).contains("Transaction Data");
        assertThat(result).contains("Coffee Shop");
        assertThat(result).contains("Grocery Store");
        assertThat(result).contains("5.50");
    }

    @Test
    void shouldThrowExceptionForUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "Some content".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> documentParserService.parseDocument(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void shouldThrowExceptionForNullFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "text/csv",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> documentParserService.parseDocument(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File name cannot be null");
    }
}
