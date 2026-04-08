package com.project.personal_assistant.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Lazy
@Service
public class NewsService {

    private final String apiKey;
    private final HttpClient client = HttpClient.newHttpClient();

    public NewsService(@Value("${news.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String getNews(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = buildUrl(encodedQuery);

            String responseBody = callApi(url);

            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray articles = root.getAsJsonArray("articles");

            // 🔥 fallback if empty
            if (articles == null || articles.size() == 0) {
                url = buildUrl("technology"); // fallback
                responseBody = callApi(url);

                root = JsonParser.parseString(responseBody).getAsJsonObject();
                articles = root.getAsJsonArray("articles");
            }

            return formatNews(articles);

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error fetching news";
        }
    }

    private String buildUrl(String query) {
        return "https://newsapi.org/v2/everything?q=" + query +
                "&sortBy=publishedAt&pageSize=10&apiKey=" + apiKey;
    }

    private String callApi(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("API RESPONSE: " + response.body()); // debug

        return response.body();
    }

    private String formatNews(JsonArray articles) {
        StringBuilder result = new StringBuilder("📰 Top News:\n\n");

        for (int i = 0; i < Math.min(5, articles.size()); i++) {
            JsonObject article = articles.get(i).getAsJsonObject();

            String title = article.get("title").getAsString();
            String source = article.getAsJsonObject("source").get("name").getAsString();
            String url = article.get("url").getAsString();

            result.append(i + 1)
                    .append(". ")
                    .append(title)
                    .append("\n")
                    .append("Source: ").append(source).append("\n")
                    .append(url)
                    .append("\n\n");
        }

        return result.toString();
    }
}