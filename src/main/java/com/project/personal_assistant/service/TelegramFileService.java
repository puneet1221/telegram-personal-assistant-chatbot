package com.project.personal_assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFileService {

    // Bot token — Telegram API call ke liye zaroori
    @Value("${telegram.bot.token}")
    private String botToken;

    // HTTP client — Telegram se file download karne ke liye
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Step 1 — fileId se file path lo Telegram API se
    // Step 2 — file path se actual file download karo
    public byte[] downloadFile(String fileId) throws Exception {

        // Telegram API se file path lo
        String fileInfoUrl = "https://api.telegram.org/bot" + botToken +
                             "/getFile?file_id=" + fileId;

        HttpRequest infoRequest = HttpRequest.newBuilder()
            .uri(URI.create(fileInfoUrl))
            .GET()
            .build();

        HttpResponse<String> infoResponse = httpClient.send(
            infoRequest, HttpResponse.BodyHandlers.ofString()
        );

        // JSON parse karo — file_path nikalo
        String body = infoResponse.body();
        // file_path extract karo response se
        String filePath = extractFilePath(body);

        log.info("File path from Telegram: {}", filePath);

        // Actual file download karo
        String downloadUrl = "https://api.telegram.org/file/bot" +
                              botToken + "/" + filePath;

        HttpRequest downloadRequest = HttpRequest.newBuilder()
            .uri(URI.create(downloadUrl))
            .GET()
            .build();

        HttpResponse<byte[]> downloadResponse = httpClient.send(
            downloadRequest, HttpResponse.BodyHandlers.ofByteArray()
        );

        log.info("File downloaded — size: {} bytes", downloadResponse.body().length);
        return downloadResponse.body();
    }

    // JSON se file_path extract karo — Gson use karo
    private String extractFilePath(String jsonResponse) {
        // Response kuch aisa hoga:
        // {"ok":true,"result":{"file_id":"...","file_path":"documents/file.pdf"}}
        int start = jsonResponse.indexOf("\"file_path\":\"") + 13;
        int end = jsonResponse.indexOf("\"", start);
        return jsonResponse.substring(start, end);
    }
}