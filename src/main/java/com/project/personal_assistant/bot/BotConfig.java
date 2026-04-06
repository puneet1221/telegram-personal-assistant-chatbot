package com.project.personal_assistant.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public TelegramBotsApi telegramBotsApi(PersonalAssistantBot bot) throws TelegramApiException {
        SetWebhook webhook = SetWebhook.builder()
                .url(webhookUrl + "/webhook")
                .build();

        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            api.registerBot(bot, webhook);
            log.info("Webhook set: {}", webhookUrl + "/webhook");
        } catch (TelegramApiException e) {
            log.error("Webhook register nahi hua: ", e);
        }
        return api;
    }
}