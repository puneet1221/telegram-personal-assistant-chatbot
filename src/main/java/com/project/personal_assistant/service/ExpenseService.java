package com.project.personal_assistant.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.repo.ExpenseRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepo expenseRepository;

    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(Long chatId) {
        return expenseRepository.findAllByChatId(chatId);
    }

    public List<Expense> getByCategoryAndChatId(String category, Long chatId) {
        return expenseRepository.findByChatIdAndCategory(chatId, category);
    }

    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }

    public boolean deleteExpenseByIndex(Long chatId, int index) {
        List<String> expenseIds = expenseRepository.findAllIdsByChatId(chatId);
        if (index < 0 || index >= expenseIds.size())
            return false;
        expenseRepository.deleteById(expenseIds.get(index));
        return true;
    }

    public boolean editExpensesByIndex(int index, Double amount, String category, String description, Long chatId) {
        List<String> expenseIds = expenseRepository.findAllIdsByChatId(chatId);
        if (index < 0 || index >= expenseIds.size())
            return false;
        Optional<Expense> expenseOptional = expenseRepository.findById(expenseIds.get(index));
        if (!expenseOptional.isPresent()) {
            return false;
        }
        Expense expense = expenseOptional.get();
        expense.setChatId(chatId);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDescription(description);
        expenseRepository.save(expense);
        return true;
    }

    public List<Expense> getTodayExpenses(Long chatId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return expenseRepository.findByChatIdAndDateBetween(chatId, startOfDay, endOfDay);
    }
}