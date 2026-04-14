package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.service.ReminderService;

import lombok.RequiredArgsConstructor;

@Component
@Order(3)
@RequiredArgsConstructor
public class RemindersListHandler implements MessageHandler {

    private final ReminderService reminderService;

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        return messageText.startsWith("/reminders");
    }

    @Override
    public String handle(Update update, String messageText) {
        long chatId = update.getMessage().getChatId();
        var reminders = reminderService.getAllReminders(chatId);
        if (reminders.isEmpty())
            return "Koi reminder nahi abhi tak!";

        StringBuilder sb = new StringBuilder("Tere reminders:\n\n");
        for (int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);

            sb.append("" + (i + 1) + "  .").append("⏰ ").append(reminder.getReminderTime())
                    .append(" — ").append(reminder.getMessage())
                    .append(reminder.isSent() ? " (sent)" : " (pending)")
                    .append("\n");
        }
        sb.append("\nTo delete a specific reminder:\n\n" +
                " delete reminder <seq_no>\n" +
                "To delete all past reminders use:\n\n" +
                " delete past reminder"

        );
        return sb.toString();
    }
}