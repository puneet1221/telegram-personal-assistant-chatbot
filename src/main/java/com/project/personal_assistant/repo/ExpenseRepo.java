package com.project.personal_assistant.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.personal_assistant.model.Expense;

public interface ExpenseRepo extends MongoRepository<Expense, String> {
    List<Expense> findByChatIdAndCategory(Long chatId, String category);

    List<String> findAllIdsByChatId(Long chatId);

    List<Expense> findByChatIdAndDateBetween(Long chatId, LocalDateTime start, LocalDateTime end);

    List<Expense> findAllByChatId(Long chatId);
}