package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.NewsService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Order(6)
@Component
@Slf4j
public class NewsHandler implements MessageHandler {
    private final NewsService newsService;

    @Override
    public boolean canHandle(String messageText) {
        log.info(messageText + " handled by News Handler");
        return messageText.trim().toLowerCase().contains("/news");
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            String[] parts = messageText.split(" ", 2);
            String query = (parts.length < 2) ? "technology" : parts[1];
            return newsService.getNews(query);
        } catch (Exception e) {
            return "something went wrong X";
        }

    }

}
