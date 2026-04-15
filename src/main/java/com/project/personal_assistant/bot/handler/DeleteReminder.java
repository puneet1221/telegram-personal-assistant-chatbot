package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ReminderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(5)
@Component
@Slf4j
@AllArgsConstructor
public class DeleteReminder implements MessageHandler {
    private final ReminderService reminderService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return (messageText.toLowerCase().startsWith("delete reminder") ||
                messageText.toLowerCase().startsWith("delete past reminder"));
    }

    @Override
    public String handle(Update update, String messageText) {
        if (messageText.startsWith("delete reminder")) {
            return deleteReminderByIndex(messageText);
        }
        if (messageText.toLowerCase().startsWith("delete past reminders")) {
            return deleteAllPastReminders(update.getMessage().getChatId());
        }
        return "\nwrong format \n" +
                " Please enter correct format as show below:\n" +
                " delete reminer 1\n" +
                " delete past reminders";
    }

    public String deleteReminderByIndex(String messageText) {
        String[] parts = messageText.toLowerCase().split(" ");
        int index;
        try {
            index = Integer.parseInt(parts[2]) - 1;
        } catch (Exception e) {
            return "\nwrong format \n" +
                    " Please enter correct format as show below:\n" +
                    " delete reminer 1\n" +
                    " delete past reminders";
        }

        return reminderService.deleteByIndex(index);
    }

    public String deleteAllPastReminders(Long chatId) {
        return reminderService.deleteAllPastReminders(chatId);
    }

}
