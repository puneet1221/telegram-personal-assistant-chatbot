package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ReminderService;

import lombok.RequiredArgsConstructor;

@Component
@Order(3)
@RequiredArgsConstructor
public class RemindersListHandler implements MessageHandler {

    private final ReminderService reminderService;

    @Override
    public boolean canHandle(String messageText) {
        return messageText.startsWith("/reminders");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();
        var reminders = reminderService.getAllReminders(chatId);
        if (reminders.isEmpty())
            return "Koi reminder nahi abhi tak!";

        StringBuilder sb = new StringBuilder("Tere reminders:\n\n");
        for (var reminder : reminders) {
            sb.append("⏰ ").append(reminder.getReminderTime())
                    .append(" — ").append(reminder.getMessage())
                    .append(reminder.isSent() ? " (sent)" : " (pending)")
                    .append("\n");
        }
        return sb.toString();
    }
}