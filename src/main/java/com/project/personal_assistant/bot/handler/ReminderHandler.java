package com.project.personal_assistant.bot.handler;

import java.time.LocalDateTime;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.google.gson.JsonObject;
import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.service.GeminiService;
import com.project.personal_assistant.service.QuartzService;
import com.project.personal_assistant.service.ReminderService;

import lombok.RequiredArgsConstructor;

@Component
@Order(5)
@RequiredArgsConstructor
public class ReminderHandler implements MessageHandler {

    private final ReminderService reminderService;
    private final QuartzService quartzService;
    private final GeminiService geminiService;

    @Override
    public boolean canHandle(String messageText) {
        JsonObject parsed = geminiService.parseUserMessage(messageText);
        return "reminder".equals(parsed.get("type").getAsString());
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            JsonObject data = geminiService.parseUserMessage(messageText);

            String datetimeStr = data.get("datetime").getAsString();
            String message = data.get("message").getAsString();
            LocalDateTime reminderTime = LocalDateTime.parse(datetimeStr);
            long chatId = update.getMessage().getChatId();

            //db me save kiya reminder
            Reminder saved = reminderService.addReminder(chatId, message, reminderTime);
            quartzService.scheduleReminder(saved.getId(), chatId, message, reminderTime);

            return "Reminder set ho gaya!\n" +
                    "Time: " + reminderTime + "\n" +
                    "Message: " + message;

        } catch (Exception e) {
            return "Reminder set nahi hua, dobara try karo!";
        }
    }
}