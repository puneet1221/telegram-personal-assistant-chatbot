package com.project.personal_assistant.service;

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

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getByCategory(String category) {
        return expenseRepository.findByCategory(category);
    }

    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }
}