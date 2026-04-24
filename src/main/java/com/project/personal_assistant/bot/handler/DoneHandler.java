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
    public boolean canHandle(String messageText, Long userId) {
        // Sirf /done command handle karo
        return messageText.equalsIgnoreCase("/done");
    }

    @Override
    public String handle(Update update, String messageText) {
        long userId = update.getMessage().getChatId();

        log.info("Session clear ho raha hai — userId: {}", userId);

        // Session completely clear karo
        // State bhi jaayegi, document ID bhi
        sessionManager.clearSession(userId);

        return "✅ Session khatam ho gaya! Main menu se kuch aur choose karo 👇";
    }

}