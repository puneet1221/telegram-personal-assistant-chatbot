package com.project.personal_assistant.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.project.personal_assistant.model.HabitLog;

@Repository
public interface HabitLogRepository extends MongoRepository<HabitLog, String> {
    List<HabitLog> findByChatIdAndDate(Long chatId, LocalDate date);
}