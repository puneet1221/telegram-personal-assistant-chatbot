package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Order(1) — sabse pehle check hona chahiye
// Kyunki QNA session mein baaki sab handlers bypass karne hain
@Order(1)
@Component
@RequiredArgsConstructor
@Slf4j
public class QnAHandler implements MessageHandler {

    // SessionManager inject karo — user ki state check karni hai
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        // Ye method chatId nahi leta — isliye hum sirf
        // /QNA:read-a-file command check kar sakte hain yahan
        // Session check PersonalAssistantBot mein hoga
        return messageText.equalsIgnoreCase("/QNA:read-a-file");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();

        log.info("QNA session start ho raha hai — chatId: {}", chatId);

        // User ki state WAITING_FOR_FILE set karo
        // Ab bot sirf file ka wait karega
        sessionManager.setState(chatId, UserState.WAITING_FOR_FILE);

        // User ko instructions bhejo
        return "📄 File upload karo!\n\n" +
               "Supported formats:\n" +
               "• PDF\n" +
               "• TXT\n" +
               "• DOCX\n\n" +
               "File upload karne ke baad tum seedha questions pooch sakte ho.\n" +
               "Session khatam karne ke liye /done likho.";
    }
}