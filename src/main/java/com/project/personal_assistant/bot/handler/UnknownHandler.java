package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(8)
@RequiredArgsConstructor
public class UnknownHandler implements MessageHandler {

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        log.debug("unknown handler invoked");
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