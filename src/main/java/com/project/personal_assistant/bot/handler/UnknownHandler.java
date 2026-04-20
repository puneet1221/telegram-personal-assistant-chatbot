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
    public boolean canHandle(String messageText,Long chatId) {
        log.info("unknown handler invoked");
        return true;
    }

    @Override
    public String handle(Update update, String messageText) {
        JsonObject object=groqChatService.parseUserMessage(messageText);
        String response=object.get("reply")+"""
        /n/n
        /start ->To get started
        /help 
        /habit
                
                """;
        log.info(object.toString());
        return response;
    }
}