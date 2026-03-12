package com.phonethantzaw.aibankingassistant.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
@Slf4j
public class DocumentParserService {

    public String parseDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> parsePdf(file);
            case "csv" -> parseCsv(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };
    }

    private String parsePdf(MultipartFile file) throws IOException {
        log.info("Parsing PDF file: {}", file.getOriginalFilename());
        
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Successfully extracted {} characters from PDF", text.length());
            return text;
        }
    }

    private String parseCsv(MultipartFile file) throws IOException {
        log.info("Parsing CSV file: {}", file.getOriginalFilename());
        
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = csvReader.readAll();
            
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            StringBuilder content = new StringBuilder();
            String[] headers = rows.get(0);
            
            content.append("Transaction Data:\n");
            content.append(String.join(", ", headers)).append("\n\n");

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                for (int j = 0; j < Math.min(headers.length, row.length); j++) {
                    content.append(headers[j]).append(": ").append(row[j]).append("\n");
                }
                content.append("\n");
            }

            String text = content.toString();
            log.info("Successfully parsed {} rows from CSV", rows.size() - 1);
            return text;
            
        } catch (CsvException e) {
            log.error("Error parsing CSV file", e);
            throw new IOException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
