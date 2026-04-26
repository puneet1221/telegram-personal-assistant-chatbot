package com.project.personal_assistant.bot.handler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.google.gson.JsonObject;
import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.service.GroqChatService;
import com.project.personal_assistant.service.QuartzService;
import com.project.personal_assistant.service.ReminderService;
import com.project.personal_assistant.service.SessionManagerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(7)
@RequiredArgsConstructor
public class ReminderHandler implements MessageHandler {

    private final SessionManagerService sessionManagerService;
    private final ReminderService reminderService;
    private final QuartzService quartzService;
    private final GroqChatService chatService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        log.info("can Reminder Handler handle this? {}", messageText);
        if (messageText.toLowerCase().startsWith("delete") ||
                messageText.toLowerCase().startsWith("edit") ||
                messageText.toLowerCase().startsWith("/")) {
            return false;
        }
        JsonObject parsed = chatService.parseUserMessage(messageText);
        String type = parsed.get("type").getAsString();
        return type.equals("reminder") || type.equals("recurring_reminder");
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            JsonObject data = chatService.parseUserMessage(messageText);
            long chatId = update.getMessage().getChatId();
            String type = data.get("type").getAsString();

            if (type.equals("recurring_reminder")) {
                return handleRecurringReminder(data, chatId);
            } else {
                return handleReminder(data, chatId);
            }
        } catch (Exception e) {
            return "Reminder set nahi hua, dobara try karo!";
        }
    }

    private String handleRecurringReminder(JsonObject data, long chatId) {
        String frequency = data.get("frequency").getAsString();
        String time = data.get("time").getAsString();
        String message = data.get("message").getAsString();

        // Weekly ke liye
        String dayOfWeek = data.has("day") &&
                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
                        .contains(data.get("day").getAsString())
                                ? data.get("day").getAsString()
                                : null;

        // Monthly ke liye — day number
        String dayOfMonth = data.has("day") && dayOfWeek == null
                ? data.get("day").getAsString()
                : "*";

        // Yearly ke liye — month
        String month = data.has("month")
                ? data.get("month").getAsString()
                : "*";

        String cronExpression = reminderService.buildCronExpression(
                frequency, time, dayOfWeek, dayOfMonth, month);

       Reminder saved= reminderService.addRecurringReminder(chatId, message, frequency, dayOfWeek, dayOfMonth, month, time,
                cronExpression);

        quartzService.scheduleRecurringReminder(
                saved.getId(), chatId, message, cronExpression);

        String response = "Recurring reminder set ho gaya!\n" +
                "Message: " + message + "\n" +
                "Frequency: " + frequency + "\n" +
                "Time: " + time;

        if (dayOfWeek != null)
            response += "\nDay: " + dayOfWeek;
        if (!"*".equals(dayOfMonth))
            response += "\nDate: " + dayOfMonth;
        if (!"*".equals(month))
            response += "\nMonth: " + month;

        return response;
    }

    public String handleReminder(JsonObject data, Long chatId) {
        String dateTimeStr = data.get("dateTime").getAsString();
        String message = data.get("message").getAsString();
        LocalDateTime reminderTime = LocalDateTime.parse(dateTimeStr);
        Reminder saved = reminderService.addReminder(chatId, message, reminderTime);
        quartzService.scheduleReminder(saved.getId(), chatId, message, reminderTime);

        return """
                Reminder set ho gya!
                Time :  %s
                Message : %s
                """.formatted(dateTimeStr, message);
    }

}