package com.project.personal_assistant.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.project.personal_assistant.model.Expense;

public interface ExpenseRepo extends MongoRepository<Expense, String> {
    List<Expense> findByChatIdAndCategory(Long chatId, String category);

    @Query(value = "{ 'chatId': ?0 }", fields = "{ '_id': 1 }")
    List<Expense> findIdsByChatId(Long chatId);

    List<String> findAllIdByChatId(Long chatId);

    List<Expense> findByChatIdAndDateBetween(Long chatId, LocalDateTime start, LocalDateTime end);

    List<Expense> findAllByChatId(Long chatId);

    void deleteByChatIdAndDescription(Long chatId, String description);
}