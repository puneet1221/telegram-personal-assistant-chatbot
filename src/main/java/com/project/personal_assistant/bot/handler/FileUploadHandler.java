package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.RAGService;
import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;
import com.project.personal_assistant.service.TelegramFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(2)
@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadHandler implements MessageHandler {

    private final SessionManagerService sessionManager;
    private final RAGService ragService;
    // Telegram file download service
    private final TelegramFileService telegramFileService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return false;
    }

    @Override
    public String handle(Update update, String messageText) {
        return handleFileUpload(update);
    }

    public String handleFileUpload(Update update) {
        long chatId = update.getMessage().getFrom().getId();

        try {
            if (!update.getMessage().hasDocument()) {
                return "❌ File nahi mili!\n\nPDF, TXT ya DOCX upload karo.";
            }

            Document document = update.getMessage().getDocument();

            String fileName = document.getFileName();
            String fileId = document.getFileId();

            log.info("File received — name: {}, fileId: {}", fileName, fileId);

            // Telegram se file download karo
            byte[] fileContent = telegramFileService.downloadFile(fileId);

            // RAGService ko process karne do
            ragService.processFile(fileContent, fileName);

            // fileName document ID ki tarah use karo
            sessionManager.setDocument(chatId, fileName);

            // QNA session start karo
            sessionManager.setState(chatId, UserState.QNA_SESSION);

            log.info("File processed — chatId: {}", chatId);

            return "✅ File upload ho gayi!\n\n" +
                    "Ab kuch bhi puchho — main is document se answer dunga.\n" +
                    "/done — session khatam karo";

        } catch (Exception e) {
            log.error("File upload error — chatId: {}", chatId, e);
            sessionManager.setState(chatId, UserState.WAITING_FOR_FILE);
            return "❌ File upload nahi hua! Dobara try karo.";
        }
    }

}