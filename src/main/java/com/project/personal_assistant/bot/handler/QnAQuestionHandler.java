package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.RAGService;
import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Order(3) — QnAHandler aur FileUploadHandler ke baad
// But PersonalAssistantBot mein session check se pehle hi
// ye handler call hoga QNA_SESSION mein
@Order(3)
@Component
@RequiredArgsConstructor
@Slf4j
public class QnAQuestionHandler implements MessageHandler {

    // RAGService — question answer karne ke liye
    private final RAGService ragService;

    // SessionManager — user ki state check karni hai
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        // /done command ye handler handle nahi karega
        // Baaki sab QNA_SESSION mein ye handle karega
    return sessionManager.getState(chatId) == UserState.QNA_SESSION
        && !messageText.equalsIgnoreCase("/done");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();

        // Session check karo — agar QNA_SESSION nahi hai toh handle mat karo
        if (sessionManager.getState(chatId) != SessionManagerService.UserState.QNA_SESSION) {
            // Ye message kisi aur handler ka hai
            return null;
        }

        log.info("QNA question — chatId: {}, question: {}", chatId, messageText);

        try {
            // RAGService se answer lo
            // Ye tera existing askQuestion() method hai
            String answer = ragService.askQuestion(messageText);

            return "🤖 " + answer + "\n\n" +
                    "_Session active hai — aur questions puchho_\n" +
                    "_/done — session khatam karo_";

        } catch (Exception e) {
            log.error("QNA question error — chatId: {}", chatId, e);
            return "❌ Answer nahi mila, dobara try karo!"
                    + "\n\n"
                    + "/done - to terminate this session";
        }
    }
}