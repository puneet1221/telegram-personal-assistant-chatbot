package com.project.personal_assistant.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BotConfig {

    @Value("${webhook.url}")
    private String webhookUrl;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi(PersonalAssistantBot bot) throws TelegramApiException {
        SetWebhook webhook = SetWebhook.builder()
                .url(webhookUrl + "/webhook")
                .build();
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            api.registerBot(bot, webhook);
        } catch (TelegramApiException e) {
            log.warn("Bot registration: {}", e.getMessage());
        }
        return api;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setWebhook() {
        try {
            String url = "https://api.telegram.org/bot" + botToken +
                    "/setWebhook?url=" + webhookUrl + "/webhook";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            log.info("Webhook set response: {}", response.body());

        } catch (Exception e) {
            log.error("Webhook set nahi hua: ", e);
        }
    }
}