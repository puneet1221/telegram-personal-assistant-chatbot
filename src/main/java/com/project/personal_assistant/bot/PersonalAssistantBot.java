package com.project.personal_assistant.bot;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.personal_assistant.bot.handler.MessageHandler;
import com.project.personal_assistant.service.GeminiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PersonalAssistantBot extends TelegramLongPollingBot {

    private final List<MessageHandler> handlers;
    private final GeminiService geminiService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public PersonalAssistantBot(
            List<MessageHandler> handlers,
            @Value("${telegram.bot.token}") String botToken, GeminiService geminiService) {
        super(botToken);
        this.handlers = handlers;
        this.geminiService = geminiService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        sendMessage(chatId, "Samajh raha hoon...");

        // calling the list of handlers
        String response = handlers.stream()
                .filter(h -> h.canHandle(messageText))
                .findFirst()
                .map(h -> h.handle(update, messageText))
                .orElse("Kuch galat hua, dobara try karo!");

        geminiService.clearCache(messageText); // cache clear karoO
        sendMessage(chatId, response);
    }

    public void sendReminderMessage(long chatId, String text) {
        sendMessage(chatId, text);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message send nahi hua: ", e);
        }
    }
}