package com.project.personal_assistant.bot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ExpenseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(5)
@Component
@RequiredArgsConstructor
public class ExpenseEditHandler implements MessageHandler {
    @Autowired
    private ExpenseService expenseService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return messageText.startsWith("edit expense");
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            String[] parts = messageText.split(" ", 6);
            if (parts.length < 6) {
                return "Format galat!\nSahi: edit expense 1 400 food snacks";
            }

            int index = Integer.parseInt(parts[2]) - 1;
            double amount = Double.parseDouble(parts[3]);
            String category = parts[4];
            String description = parts[5];

            boolean edited = expenseService.editExpensesByIndex(index, amount, category, description,
                    update.getMessage().getFrom().getId());
            if (edited) {
                return "Expense update ho gaya!\n" +
                        "Amount: Rs." + amount + "\n" +
                        "Category: " + category + "\n" +
                        "Description: " + description;
            } else {
                return "Ye number exist nahi karta — /expenses se list dekho!";
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return "Format sahi nahi!\nExample: edit expense 1 400 food snacks";
        }

    }

}