package com.project.personal_assistant.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.repo.ReminderRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ReminderRecoveryService {

    private final ReminderRepository reminderRepository;

    private final QuartzService quartzService;

    /*
     * @EventListener(ApplicationReadyEvent.class) — Spring Boot fully start hone ke
     * baad ye method automatically call hoga. Database ready, Quartz ready — sab
     * kuch ready hone ke baad recovery hogi.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverPendingReminders() {
        try {
            log.info("Recovering pending reminders...");

            List<Reminder> pendingReminders = reminderRepository.findBySentFalse();

            if (pendingReminders == null || pendingReminders.isEmpty()) {
                log.info("No pending reminders to recover");
            } else {
                int recovered = 0;
                int expired = 0;

                for (Reminder reminder : pendingReminders) {
                    try {
                        // Validate reminder fields
                        if (reminder == null || reminder.getId() == null
                                || reminder.getChatId() == null
                                || reminder.getReminderTime() == null
                                || reminder.getMessage() == null) {
                            log.warn("Skipping reminder with null fields: {}", reminder);
                            continue;
                        }

                        if (reminder.getReminderTime().isAfter(LocalDateTime.now())) {
                            quartzService.scheduleReminder(
                                    reminder.getId(),
                                    reminder.getChatId(),
                                    reminder.getMessage(),
                                    reminder.getReminderTime());
                            recovered++;
                            log.info("Recovered: {} at {}", reminder.getMessage(), reminder.getReminderTime());
                        } else {
                            reminder.setSent(true);
                            reminderRepository.save(reminder);
                            expired++;
                            log.info("Expired reminder marked sent: {}", reminder.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("Error processing individual reminder during recovery: {}", reminder, e);
                        // Continue with next reminder
                    }
                }

                log.info("Recovery complete — Recovered: {}, Expired: {}", recovered, expired);
            }

            // Schedule Daily summary
            try {
                quartzService.scheduleDailySummary();
                log.info("Daily summary scheduled successfully");
            } catch (Exception e) {
                log.error("Failed to schedule daily summary: ", e);
            }

        } catch (Exception e) {
            log.error("Error during reminder recovery process: ", e);
            // Don't throw exception to prevent application startup failure
        }

        // testing
        // dailySummaryService.sendSummaryToAllUsers(personalAssistantBot);
    }
}