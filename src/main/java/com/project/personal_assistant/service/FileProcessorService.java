package com.project.personal_assistant.service;

import java.io.ByteArrayInputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileProcessorService {
    private final Tika tika = new Tika();

    public String extract(byte[] fileContent, String fileName) {

        try {
            log.info("Extracting text from {}", fileName);
            String text = tika.parseToString(new ByteArrayInputStream(fileContent));
            return text;
        } catch (Exception e) {
            log.error("Error parsing file {}: {}", fileName, e.getMessage(), e);
            return null;
        }

    }

    public boolean isValidFileType(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        // Common supported formats
        return lower.endsWith(".pdf") ||
                lower.endsWith(".txt") ||
                lower.endsWith(".docx") ||
                lower.endsWith(".doc") ||
                lower.endsWith(".xlsx") ||
                lower.endsWith(".xls") ||
                lower.endsWith(".pptx") ||
                lower.endsWith(".ppt") ||
                lower.endsWith(".html") ||
                lower.endsWith(".xml") ||
                lower.endsWith(".json");

    }
}
