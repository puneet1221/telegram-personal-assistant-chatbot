package com.project.personal_assistant.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.personal_assistant.model.Reminder;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByChatId(Long chatId);

    List<Reminder> findBySentFalse();

    List<Reminder> findByChatIdAndReminderTimeBetweenAndSentFalse(
            Long chatId, LocalDateTime start, LocalDateTime end);
}