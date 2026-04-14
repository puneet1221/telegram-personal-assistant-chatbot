package com.project.personal_assistant.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class RAGService {
    private final VectorStore vectorStore;
    /*
     * --future enhancement
     * private final ChatModel chatModel;
     */

    /*
     * temporarilry
     */
    private final GroqChatService groqChatService;
    private final FileProcessorService fileProcessor;
    /*
     * TokenTextSplitter optimized for Groq's large context window
     * 2000: Max tokens per chunk (optimized for Groq's 128K context)
     * 300: Overlap tokens (maintains cross-chunk context)
     * 5: Min characters (discard very small/useless chunks)
     * 10000: Max total chunks (safety guard to prevent memory crash)
     * true: Keep separators (preserves original formatting/newlines)
     */
    private final TokenTextSplitter splitter = new TokenTextSplitter(2000, 300, 5, 10000, true);
    
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB limit

    /**
     * Process file using Apache Tika and store chunks in MongoDB
     * Includes file size validation and semantic-aware chunking
     */
    public void processFile(byte[] fileContent, String fileName) {
        try {
            log.info("Processing file: {}", fileName);
            
            // ✅ Validate file size to prevent memory issues
            if (fileContent.length > MAX_FILE_SIZE) {
                throw new RuntimeException(
                    String.format("File exceeds maximum size limit of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
            }

            // ✅ Extract text using Tika (works for ANY format)
            String extractedText = fileProcessor.extract(fileContent, fileName);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new RuntimeException("No text extracted from file");
            }

            // Create document with persistent metadata
            Map<String, Object> metadata = Map.of(
                    "fileName", fileName,
                    "fileSize", String.valueOf(fileContent.length),
                    "processedAt", String.valueOf(System.currentTimeMillis()));
            
            Document rawDoc = new Document(extractedText, metadata);

            // Split into semantic chunks with metadata preservation
            List<Document> chunks = splitter.apply(List.of(rawDoc));
            
            // // Har chunk par file ki details (name, size, time) ka stamp lagana taaki pehchan sakein ki ye kis file ka tukda hai.
            chunks.forEach(chunk -> chunk.getMetadata().putAll(metadata));

            // Store in MongoDB via VectorStore
            vectorStore.accept(chunks);

            log.info("✅ Successfully processed {} into {} chunks (avg ~{} tokens)", 
                    fileName, chunks.size(), extractedText.length() / chunks.size() / 4);

        } catch (Exception e) {
            log.error("Error processing file: {}", fileName, e);
            throw new RuntimeException("Failed to process file: " + e.getMessage());
        }
    }

    /**
     * Ask a question and get answer based on stored documents
     */
    public String askQuestion(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "❌ Please provide a valid question.";
        }

        try {
            log.info("Processing question: {}", query);

            // Search for similar chunks
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.query(query).withTopK(3));

            // Check if relevant documents found
            if (similarDocs.isEmpty()) {
                log.warn("No relevant documents found for query: {}", query);
                return "❌ I couldn't find any relevant information to answer your question. " +
                        "Please upload documents first.";
            }

            // Combine chunks with separator
            String context = similarDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n---\n\n"));

            // Build prompt
            String promptText = """
                    You are a helpful assistant. Use the following context to answer the user's question.
                    If you don't know the answer based on the context, just say you don't know.

                    CONTEXT:
                    {context}

                    USER QUESTION:
                    {userQuery}
                    """.replace("{context}", context)
                    .replace("{userQuery}", query);

            /*
             * Future enhancement
             * -----------------------------
             * PromptTemplate template = new PromptTemplate(promptText);
             * Map<String, Object> model = Map.of(
             * "context", context,
             * "userQuery", query);
             * Prompt prompt = template.create(model);
             * 
             * // Get response from LLM
             * String response = chatModel.call(prompt)
             * .getResult()
             * .getOutput()
             * .getContent();
             * 
             * 
             * 
             */

            log.info("✅ Generated response for query");
            return groqChatService.generateAnswer(promptText);

        } catch (Exception e) {
            log.error("Error answering question", e);
            return "❌ Error processing your question: " + e.getMessage();
        }
    }
}