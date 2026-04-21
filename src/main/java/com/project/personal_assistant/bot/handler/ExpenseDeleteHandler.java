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
@Component
@Order(4)
@RequiredArgsConstructor 
public class ExpenseDeleteHandler implements MessageHandler {

    private final ExpenseService expenseService;
    private final SessionManagerService sessionManager;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return sessionManager.getState(chatId) == UserState.WAITING_FOR_DELETE_EXPENSE;
    }

    @Override
    public String handle(Update update, String messageText) {
        Long chatId = update.getMessage().getChatId(); // ✅ chatId

        try {
            int index = sessionManager.getPendingIndex(chatId);

            boolean deleted = expenseService.deleteExpenseByIndex(index, chatId);

            sessionManager.setState(chatId, UserState.NORMAL); // ✅ reset

            if (deleted) {
                return "🗑️ Expense delete ho gaya!";
            } else {
                return "❌ Expense nahi mila! /expenses se list dekho.";
            }
        } catch (Exception e) {
            log.error("Delete error: {}", e.getMessage());
            return "❌ Kuch galat hua! Dobara try karo.";
        }
    }
}