package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Order(1)
@Component
public class StartHandler implements MessageHandler {

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        return messageText.startsWith("/start");
    }

    @Override
    public String handle(Update update, String messageText) {
        return "Jai ho Puneet!\n\n" +
                "Ab seedha bol kya chahiye!\n\n" +
                "Examples:\n" +
                "aaj 500 khane pe kharch kiye\n" +
                "kal subah 8 baje gym yaad dilana\n" +
                "/expenses — sab expenses\n" +
                "/reminders — sab reminders";
    }
}