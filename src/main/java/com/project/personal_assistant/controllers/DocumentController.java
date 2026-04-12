package com.project.personal_assistant.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.personal_assistant.service.FileProcessorService;
import com.project.personal_assistant.service.RAGService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@RestController
@RequestMapping("/api/rag")
@Slf4j
public class DocumentController {

    private final RAGService ragService;
    private final FileProcessorService fileProcessor;

    /**
     * Upload any document (PDF, DOCX, XLSX, PPT, TXT, etc.)
     * 
     * Usage: POST /api/rag/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ File is empty!");
            }

            String fileName = file.getOriginalFilename();

            // Validate file type
            if (!fileProcessor.isValidFileType(fileName)) {
                return ResponseEntity.badRequest()
                        .body("❌ Unsupported file type. Supported: PDF, DOCX, XLSX, PPT, TXT, etc.");
            }

            // Get file content
            byte[] fileContent = file.getBytes();

            // Process the file using RAGService
            ragService.processFile(fileContent, fileName);

            return ResponseEntity.ok(
                    "✅ Document '" + fileName + "' processed and indexed successfully!\n" +
                            "You can now ask questions about it.");

        } catch (Exception e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error processing file: " + e.getMessage());
        }
    }

    /**
     * Ask a question about uploaded documents
     * 
     * Usage: GET /api/rag/ask?question=your%20question
     */
    @GetMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestParam String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("❌ Question cannot be empty!");
            }

            String answer = ragService.askQuestion(question);
            return ResponseEntity.ok(answer);

        } catch (Exception e) {
            log.error("Error answering question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ RAG Service is running!");
    }
}