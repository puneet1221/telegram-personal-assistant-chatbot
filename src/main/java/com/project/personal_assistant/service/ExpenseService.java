package com.project.personal_assistant.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    public boolean deleteExpenseByIndex(int index, Long chatId) {
        List<Expense> expenses = expenseRepository.findIdsByChatId(chatId); // ✅ sirf IDs
        if (index < 0 || index >= expenses.size())
            return false;
        expenseRepository.deleteById(expenses.get(index).getId());
        return true;
    }

    public boolean editExpensesByIndex(int index, Double amount, String category,
            String description, Long chatId) {
        List<Expense> expenses = expenseRepository.findIdsByChatId(chatId); // ✅ sirf IDs
        if (index < 0 || index >= expenses.size())
            return false;

        // ID se full document fetch karo — sirf ek record
        Expense expense = expenseRepository.findById(expenses.get(index).getId()).orElse(null);
        if (expense == null)
            return false;

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