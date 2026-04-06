package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ExpenseService;

import lombok.RequiredArgsConstructor;
@Order(2)
@Component
@RequiredArgsConstructor
public class ExpensesListHandler implements MessageHandler {

    private final ExpenseService expenseService;

    @Override
    public boolean canHandle(String messageText) {
        return messageText.startsWith("/expenses");
    }

    @Override
    public String handle(Update update, String messageText) {
        var expenses = expenseService.getAllExpenses();
        if (expenses.isEmpty())
            return "Koi expense nahi abhi tak!";

        StringBuilder sb = new StringBuilder("Teri expenses:\n\n");
        double total = 0;
        for (var expense : expenses) {
            sb.append("Rs.").append(expense.getAmount())
                    .append(" — ").append(expense.getCategory())
                    .append(" (").append(expense.getDescription()).append(")\n");
            total += expense.getAmount();
        }
        sb.append("\nTotal: Rs.").append(total);
        return sb.toString();
    }
}