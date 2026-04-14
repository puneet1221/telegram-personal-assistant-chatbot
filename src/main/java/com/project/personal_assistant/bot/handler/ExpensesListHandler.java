package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.service.ExpenseService;

import lombok.RequiredArgsConstructor;

@Order(2)
@Component
@RequiredArgsConstructor
public class ExpensesListHandler implements MessageHandler {

    private final ExpenseService expenseService;

    @Override
    public boolean canHandle(String messageText,Long chatId) {
        return messageText.startsWith("/expenses");
    }

    @Override
    public String handle(Update update, String messageText) {
        var expenses = expenseService.getAllExpenses();
        if (expenses.isEmpty())
            return "Koi expense nahi abhi tak!";

        StringBuilder sb = new StringBuilder("Teri expenses:\n\n");
        double total = 0;
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);

            Double amount = expense.getAmount() != null ? expense.getAmount() : 0.0;
            String category = expense.getCategory() != null ? expense.getCategory() : "unknown";
            String description = expense.getDescription() != null ? expense.getDescription() : "";

            sb.append(i + 1).append(". Rs.").append(amount)
                    .append(" — ").append(category)
                    .append(" (").append(description).append(")\n");
            total += amount;
        }
        sb.append("\nTotal: Rs.").append(total);
        sb.append("\n To delete an expense : delete expense expense1");
        sb.append("\n To edit an expense : edit expense 1 400 food snacks");
        return sb.toString();
    }
}