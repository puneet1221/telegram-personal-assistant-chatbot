package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;

@Component
@Order(6)
@RequiredArgsConstructor
public class UnknownHandler implements MessageHandler {

    @Override
    public boolean canHandle(String messageText) {
        return true;
    }

    @Override
    public String handle(Update update, String messageText) {
        return "Samjha nahi bhai!\n\n" +
                "Ye try karo:\n" +
                "aaj 500 khane pe kharch kiye\n" +
                "kal subah 8 baje gym yaad dilana\n" +
                "/expenses\n" +
                "/reminders";
    }
}