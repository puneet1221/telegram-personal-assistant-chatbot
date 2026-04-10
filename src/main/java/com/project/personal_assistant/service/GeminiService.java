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

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final Map<String, JsonObject> cache = new ConcurrentHashMap<>();

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=";

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

            Agar koi habit  user ne complete kiya h hai toh:
            {"type":habit_done,"habit":gym}

    
            Agar kuch aur hai toh:
            {"type": "unknown", "reply": "yahan reply likho"}

            Categories expense ke liye: food, transport, shopping, health, entertainment, other

            Datetime hamesha ISO format mein: yyyy-MM-ddTHH:mm:ss
            Agar "kal" likha hai toh agle din ki date lagao.
            Agar "subah" likha hai toh 08:00, "dopahar" toh 13:00, "shaam" toh 18:00, "raat" toh 21:00.

            Day of week hamesha uppercase English mein: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY

            Recurring reminder keywords: "har din", "daily", "har hafte", "weekly", "har Monday" etc.

            Aaj ki date: """
            + java.time.LocalDate.now() + """
                    """;

    public JsonObject parseUserMessage(String userMessage) {
        return cache.computeIfAbsent(userMessage, this::callGeminiAndParse);
        // (userMessage, (msg)->callGeminiAndParse(msg))
        // equivalent to
        /*
         * javacache.computeIfAbsent(userMessage, new Function<String, JsonObject>() {
         * 
         * @Override
         * public JsonObject apply(String msg) {
         * return callGeminiAndParse(msg);
         * }
         * });
         * computeIfAbsent(String ,Function<K,V>)
         * 
         * computeIfAbsent ensures: key automatically mapping function/method me
         * argument ke roop me jaati hai, isliye tumhe manually pass karne ki zarurat
         * nahi.
         * 
         */
    }

    public void clearCache(String userMessage) {
        cache.remove(userMessage);
        log.debug("Cache cleared for: {}", userMessage);
    }

    private JsonObject callGeminiAndParse(String userMessage) {
        try {
            log.info("Gemini API call for: {}", userMessage);
            JsonObject requestBody = buildRequest(userMessage);
            String response = callGemini(requestBody);
            return extractJson(response);
        } catch (Exception e) {
            log.error("Gemini error: ", e);
            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "unknown");
            fallback.addProperty("reply", "Samjha nahi bhai, thoda aur clear batao!");
            return fallback;
        }
    }

    private JsonObject buildRequest(String userMessage) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", SYSTEM_PROMPT + "\n\nUser: " + userMessage);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        return requestBody;
    }

    private String callGemini(JsonObject requestBody) throws Exception {
        String url = GEMINI_URL + java.net.URLEncoder.encode(
                apiKey, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private JsonObject extractJson(String geminiResponse) {
        try {
            log.info("Gemini raw response: {}", geminiResponse);

            JsonObject responseObj = gson.fromJson(geminiResponse, JsonObject.class);

            if (responseObj.has("error")) {
                log.error("Gemini API error: {}", responseObj.get("error"));
                JsonObject fallback = new JsonObject();
                fallback.addProperty("type", "unknown");
                fallback.addProperty("reply", "API error aaya, dobara try karo!");
                return fallback;
            }

            String text = responseObj
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            text = text.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return gson.fromJson(text, JsonObject.class);

        } catch (Exception e) {
            log.error("JSON parse error. Raw response: {}", geminiResponse);
            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "unknown");
            fallback.addProperty("reply", "Samjha nahi, dobara try karo!");
            return fallback;
        }
    }
}