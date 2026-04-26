package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.google.gson.JsonObject;
import com.project.personal_assistant.service.GroqChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(8)
@RequiredArgsConstructor
public class UnknownHandler implements MessageHandler {

    private final GroqChatService groqChatService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        log.info("UnknownHandler invoked for: {}", messageText);
        return true;
    }

    @Override
    public String handle(Update update, String messageText) {
        String lower = messageText.toLowerCase();

        // ✅ Feature related — guide karo
        if (lower.contains("expense") || lower.contains("kharcha")) {
            return "💰 Expense Manager:\n\n" +
                    "➕ Add: 'aaj 500 khane pe kharch kiya'\n" +
                    "📋 View/Edit/Delete: '💰 Expenses' menu se";
        }

        if (lower.contains("reminder") || lower.contains("yaad")) {
            return "⏰ Reminder Manager:\n\n" +
                    "➕ Add: 'kal subah 8 baje gym'\n" +
                    "📋 View/Delete: '⏰ Reminders' menu se";
        }

        if (lower.contains("habit")) {
            return "📅 Habit Tracker:\n\n" +
                    "'📅 Habits' menu se manage karo";
        }

        if (lower.contains("weather") || lower.contains("mausam")) {
            return "☁️ Weather:\n\n" +
                    "'☁️ Weather' menu se city enter karo";
        }

        if (lower.contains("news")) {
            return "📰 News:\n\n" +
                    "'📰 News' menu se category choose karo";
        }

        if (lower.contains("pdf") || lower.contains("document") || lower.contains("file")) {
            return "📄 DOC Q&A:\n\n" +
                    "'📄 DOC Q&A' menu se file upload karo";
        }

        try {
            JsonObject object = groqChatService.parseUserMessage(messageText);
            String reply = object.get("reply").getAsString(); // ✅ getAsString — quotes nahi aayenge
            log.info("Groq reply: {}", reply);
            return reply;
        } catch (Exception e) {
            log.error("Groq error: {}", e.getMessage());
            return "❌ Samajh nahi aaya! Main menu se choose karo 👇";
        }
    }
}