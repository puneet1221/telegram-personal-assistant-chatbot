package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.SessionManagerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Order(1) — sabse pehle check hona chahiye
// Kyunki /done QNA session mein bhi kaam karna chahiye
@Order(1)
@Component
@RequiredArgsConstructor
@Slf4j
public class DoneHandler implements MessageHandler {

    // SessionManager — session clear karne ke liye
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        // Sirf /done command handle karo
        return messageText.equalsIgnoreCase("/done");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();

        log.info("Session clear ho raha hai — chatId: {}", chatId);

        // Session completely clear karo
        // State bhi jaayegi, document ID bhi
        sessionManager.clearSession(chatId);

        return "✅ Session khatam ho gaya!\n\n" +
                "Available commands:\n" +
                "💰 expense — aaj 500 khane pe kharch kiye\n" +
                "⏰ reminder — kal subah 8 baje gym\n" +
                "📋 /expenses — sab expenses\n" +
                "🔔 /reminders — sab reminders\n" +
                "📄 /QNA:read-a-file — document Q&A\n" +
                "📊 /today — aaj ke habits\n" +
                "❓ /help — help";
    }

   
}