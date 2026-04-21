package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ExpenseService;
import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(5)
@Component
@RequiredArgsConstructor
public class ExpenseEditHandler implements MessageHandler {

    private final ExpenseService expenseService;
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return sessionManager.getState(chatId) == UserState.WAITING_FOR_EDIT_EXPENSE;
    }

    @Override
    public String handle(Update update, String messageText) {
        Long chatId = update.getMessage().getChatId(); // ✅ chatId

        try {
            String[] parts = messageText.split(" ", 3);
            if (parts.length < 3) {
                return "Format galat!\nExample: 400 food lunch";
            }

            double amount = Double.parseDouble(parts[0]);
            String category = parts[1];
            String description = parts[2];

            int index = sessionManager.getPendingIndex(chatId);

            boolean edited = expenseService.editExpensesByIndex(
                    index, amount, category, description, chatId);

            sessionManager.setState(chatId, UserState.NORMAL); // ✅ reset

            if (edited) {
                return "✅ Expense update ho gaya!\n" +
                        "Amount: ₹" + amount + "\n" +
                        "Category: " + category + "\n" +
                        "Description: " + description;
            } else {
                return "❌ Expense nahi mila!";
            }
        } catch (Exception e) {
            log.error("Edit error: {}", e.getMessage());
            return "Format sahi nahi!\nExample: 400 food lunch";
        }
    }
}