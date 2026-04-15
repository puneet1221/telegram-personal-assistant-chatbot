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

    public List<Reminder> getAllReminders(Long chatId) {
        return reminderRepository.findByChatId(chatId);
    }

    public String deleteByIndex(int index) {
        List<Reminder> reminders = reminderRepository.findAll();
        if (index < 0 || index >= reminders.size()) {
            return "Please enter a valid reminder number";
        }
        reminderRepository.deleteById(reminders.get(index).getId());
        return "Reminder deleted successfully. use /reminders to get list";
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
}