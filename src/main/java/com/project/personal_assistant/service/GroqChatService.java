package com.project.personal_assistant.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, JsonObject> cache = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            Tu ek personal assistant hai. User ka message parse karke SIRF JSON return kar.
            Koi extra text nahi, sirf JSON.

            Agar normal expense hai toh:
            {"type": "expense", "amount": 500, "category": "food", "description": "lunch"}

            Agar normal reminder hai toh:
            {"type": "reminder", "datetime": "2026-04-02T08:00:00", "message": "gym jaana hai"}

            Agar recurring reminder hai toh:
            Daily: {"type": "recurring_reminder", "frequency": "daily", "time": "07:00", "message": "gym jaana hai"}
            Weekly: {"type": "recurring_reminder", "frequency": "weekly", "day": "MONDAY", "time": "09:00", "message": "meeting hai"}

            Agar koi habit user ne complete kiya hai toh:
            {"type": "habit_done", "habit": "gym"}

            Agar kuch aur hai toh:
            {"type": "unknown", "reply": "yahan reply likho"}

            Categories expense ke liye: food, transport, shopping, health, entertainment, other
            Datetime ISO format: yyyy-MM-ddTHH:mm:ss
            Aaj ki date: """
            + java.time.LocalDate.now();

    /**
     * Get answer from Groq API
     * 
     * Models available:
     * - mixtral-8x7b-32768 (Fast & Good)
     * - llama2-70b-4096 (More accurate)
     * - gemma-7b-it (Lighter)
     */

    public JsonObject parseUserMessage(String userMessage) {
        return cache.computeIfAbsent(userMessage, this::callGroqAndParse);
    }

    public void clearCache(String userMessage) {
        cache.remove(userMessage);
        log.debug("Cache cleared for: {}", userMessage);
    }

    private JsonObject callGroqAndParse(String userMessage) {
        try {
            log.info("Calling Groq API for Intent Parsing: {}", userMessage);

            // Create Groq Request (OpenAI Format)
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", 0.1); // Precision ke liye temperature kam rakha hai

            JsonArray messages = new JsonArray();
            // System Role
            messages.add(createMessage("system", SYSTEM_PROMPT));
            // User Role
            messages.add(createMessage("user", userMessage));

            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(GROQ_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error: {} - {}", response.statusCode(), response.body());
                return createFallbackResponse("API Error aaya bhai, thoda wait kar.");
            }

            return extractJsonFromResponse(response.body());

        } catch (Exception e) {
            log.error("Error in GroqChatService", e);
            return createFallbackResponse("Kuch phat gaya bhai backend pe!");
        }
    }

    public JsonObject extractJsonFromResponse(String responseBody) {

        try {
            Gson gson = new Gson();
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            String content = responseJson.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // Cleaning markdown backticks if Groq adds them
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();

            return gson.fromJson(content, JsonObject.class);
        } catch (Exception e) {
            log.error("JSON Extraction failed from Groq response", e);
            return createFallbackResponse("Samjha nahi bhai, dobara try kar.");
        }
    }

    public JsonObject createMessage(String role, String content) {
        JsonObject msg = new JsonObject();
        msg.addProperty("role", role);
        msg.addProperty("content", content);
        return msg;
    }

    public JsonObject createFallbackResponse(String reply) {
        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "unknown");
        fallback.addProperty("reply", reply);
        return fallback;
    }

    public String generateAnswer(String prompt) {

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