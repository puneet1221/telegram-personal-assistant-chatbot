package com.project.personal_assistant.service;

import java.io.ByteArrayInputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileProcessorService {
    private final Tika tika;

    public FileProcessorService() {
        try {
            // Initialize Tika with default config
            // This may trigger reflection access if JVM args aren't set
            log.info("Initializing Tika for document parsing...");
            this.tika = new Tika();
        } catch (Exception e) {
            log.warn("Error during Tika initialization: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Tika. Ensure JVM args: --add-opens java.base/java.nio.charset=ALL-UNNAMED", e);
        }
    }

    public String extract(byte[] fileContent, String fileName) {

        try {
            log.info("Extracting text from {}", fileName);
            String text = tika.parseToString(new ByteArrayInputStream(fileContent));
            return text;
        } catch (Exception e) {
            log.error("Error parsing file {}: {}", fileName, e.getMessage(), e);
            // Re-throw with more context if it's an InaccessibleObjectException
            if (e.getCause() != null && e.getCause().getClass().getSimpleName().contains("InaccessibleObject")) {
                throw new RuntimeException(
                    "Module access denied for Tika. Ensure JVM is started with: " +
                    "--add-opens java.base/java.nio.charset=ALL-UNNAMED " +
                    "--add-opens java.base/java.lang=ALL-UNNAMED " +
                    "--add-opens java.base/java.util=ALL-UNNAMED", e);
            }
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

