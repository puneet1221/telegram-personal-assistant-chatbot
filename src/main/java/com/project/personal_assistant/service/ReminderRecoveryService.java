package com.project.personal_assistant.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.repo.ReminderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
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
        log.info("Recovring pending reminders...");

        List<Reminder> pendingReminders = reminderRepository
                .findBySentFalse();

        int recovered = 0;
        int expired = 0;

        for (Reminder reminder : pendingReminders) {
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
        }

        log.info("Recovery complete — Recovered: {}, Expired: {}", recovered, expired);
    }
}