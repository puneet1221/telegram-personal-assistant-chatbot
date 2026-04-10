package com.project.personal_assistant.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.personal_assistant.model.Habit;

@Repository
public interface HabitRepository extends MongoRepository<Habit, String> {
    @Query("{ 'chatId': ?0, 'active': true }")
    List<Habit> findByChatIdAndActive(Long chatId, Boolean active);
}