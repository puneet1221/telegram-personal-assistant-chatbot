package com.project.personal_assistant.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.repo.ReminderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public Reminder addReminder(Long chatId, String message, LocalDateTime time) {
        Reminder reminder = new Reminder();
        reminder.setChatId(chatId);
        reminder.setMessage(message);
        reminder.setReminderTime(time);
        return reminderRepository.save(reminder);
    }

    public Reminder addRecurringReminder(Long chatId, String message,
            String frequency, String dayOfWeek,
            String dayOfMonth, String month,
            String timeOfDay, String cronExpression) {
        Reminder reminder = new Reminder();

        // Basic fields
        reminder.setChatId(chatId);
        reminder.setMessage(message);

        // Recurring fields
        reminder.setRecurring(true);
        reminder.setFrequency(frequency);
        reminder.setCronExpression(cronExpression);
        reminder.setTimeOfDay(timeOfDay);

        // Frequency specific fields
        reminder.setDayOfWeek(dayOfWeek); // weekly ke liye — MONDAY etc
        reminder.setDayOfMonth(dayOfMonth); // monthly ke liye — 15 etc
        reminder.setMonth(month); // yearly ke liye — JANUARY etc

        // Recurring reminder ka reminderTime current time set karo
        // Actual firing Quartz handle karega
        reminder.setReminderTime(LocalDateTime.now());

        // sent = false — recurring reminder kabhi sent mark nahi hoga permanently
        reminder.setSent(false);
        return reminderRepository.save(reminder);
    }

    public List<Reminder> getAllReminders(Long chatId) {
        return reminderRepository.findByChatId(chatId);
    }

    public String deleteByIndex(int index, Long chatId) {
        List<Reminder> reminders = reminderRepository.findByChatId(chatId); // ✅
        if (index < 0 || index >= reminders.size()) {
            return "Please enter a valid reminder number";
        }
        reminderRepository.deleteById(reminders.get(index).getId());
        return "Reminder deleted!";
    }

    public String deleteAllPastReminders(Long chatId) {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pastReminders = reminderRepository.findRemindersToProcess(chatId, now);
        reminderRepository.deleteAll(pastReminders);
        return "All Past reminders cleared!. use /reminder to get List of reminders";
    }

    public void markSentById(String id) {
        reminderRepository.findById(id).ifPresent(reminder -> {
            reminder.setSent(true);
            reminderRepository.save(reminder);
        });
    }

    public List<Reminder> getRemindersBetween(Long chatId, LocalDateTime start, LocalDateTime end) {
        return reminderRepository.findByChatIdAndReminderTimeBetweenAndSentFalse(
                chatId, start, end);
    }

    public String buildCronExpression(String frequency, String time,
            String dayOfWeek, String dayOfMonth,
            String month) {
        String[] timeParts = time.split(":");
        String hour = timeParts[0];
        String minute = timeParts[1];

        return switch (frequency) {
            // Har din — day, month, weekday wildcard
            case "daily" ->
                "0 " + minute + " " + hour + " * * ?";

            // Har hafte — specific weekday
            case "weekly" ->
                "0 " + minute + " " + hour + " ? * " + dayOfWeek;

            // Har mahine — specific day of month
            case "monthly" ->
                "0 " + minute + " " + hour + " " + dayOfMonth + " * ?";

            // Har saal — specific month aur day
            case "yearly" ->
                "0 " + minute + " " + hour + " " + dayOfMonth + " " + month + " ?";

            // Default — daily
            default ->
                "0 0 9 * * ?";
        };
    }
}