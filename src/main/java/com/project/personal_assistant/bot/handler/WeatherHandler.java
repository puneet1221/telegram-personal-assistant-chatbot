package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;
import com.project.personal_assistant.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(6)
@RequiredArgsConstructor
public class WeatherHandler implements MessageHandler {

    private final SessionManagerService sessionManagerService;
    private final WeatherService weatherService;
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return sessionManagerService.getState(chatId).equals(UserState.WAITING_FOR_CITY)
                || messageText.startsWith("/weather");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();
        if (sessionManager.getState(chatId) == UserState.WAITING_FOR_CITY) {
            sessionManager.setState(chatId, UserState.NORMAL);
            return weatherService.getWeather(messageText.trim().toLowerCase());
        }
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            return "Please enter valid format:\n\n/weather Mumbai";
        }
        return weatherService.getWeather(parts[1].toLowerCase());
    }

}