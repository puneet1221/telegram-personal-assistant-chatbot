package com.project.personal_assistant.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroqChatService {
    @Value("${groq.api.key}")
    private String apiKey;
    @Value("${groq.model}")
    private String model;
    private String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    /**
     * Get answer from Groq API
     * 
     * Models available:
     * - mixtral-8x7b-32768 (Fast & Good)
     * - llama2-70b-4096 (More accurate)
     * - gemma-7b-it (Lighter)
     */
    public String generateAnswer(String prompt) {

        HttpClient httpClient = HttpClient.newHttpClient();
        Gson gson = new Gson();
        try {
            log.info("Calling Groq API with model: {}", model);

            // Create request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 1024);

            // Add message
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            requestBody.add("messages", messages);

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(GROQ_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {} - {}", response.statusCode(), response.body());
                return "❌ Error calling Groq API: " + response.statusCode();
            }

            // Parse response
            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
            log.info("Groq API response: {}", responseJson);
            String answer = responseJson
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            log.info("✅ Got response from Groq");
            return answer;

        } catch (Exception e) {
            log.error("Error generating answer", e);
            return "❌ Error: " + e.getMessage();
        }
    }

}