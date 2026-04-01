package com.project.personal_assistant.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for setting up the Telegram bot integration.
 * This class provides the necessary beans to initialize and register the
 * Telegram bot
 * with the Telegram Bots API, enabling the application to interact with
 * Telegram.
 */
@Slf4j
@Configuration
public class BotConfig {

    /**
     * Creates and configures the Telegram Bots API instance.
     * This bean initializes the TelegramBotsApi with a default bot session and
     * registers
     * the PersonalAssistantBot, allowing the application to receive and send
     * messages
     * through the Telegram platform.
     *
     * @param bot the PersonalAssistantBot instance to register
     * @return configured TelegramBotsApi instance
     * @throws TelegramApiException if there's an error initializing the API
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(PersonalAssistantBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        try {
            api.registerBot(bot);
        } catch (Exception e) {
            log.warn("Bot already registered or failed to register: {}", e.getMessage());
        }

        return api;
    }
}