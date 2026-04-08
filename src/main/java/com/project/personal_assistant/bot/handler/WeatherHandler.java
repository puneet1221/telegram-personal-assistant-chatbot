package com.project.personal_assistant.bot.handler;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.WeatherService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(6)
@AllArgsConstructor
public class WeatherHandler implements MessageHandler {

    private WeatherService weatherService;

    @Override
    public boolean canHandle(String messageText) {
        log.info("update handled by weather handler of order {}",6);
        return messageText.toLowerCase().startsWith("/weather");
        
    }

    @Override
    public String handle(Update update, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            return "Please enter valid format:\n\n" +
                    "/weather  Mumbai";
        }
        String cityName = parts[1];
        return weatherService.getWeather(cityName.toLowerCase());
    }

}