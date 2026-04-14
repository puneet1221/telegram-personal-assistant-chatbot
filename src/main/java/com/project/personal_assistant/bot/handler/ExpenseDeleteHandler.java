package com.project.personal_assistant.bot.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.service.ExpenseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(4)
@AllArgsConstructor
public class ExpenseDeleteHandler implements MessageHandler {
    @Autowired
    private ExpenseService expenseService;

    @Override
    public boolean canHandle(String messageText ,Long chatId) {
        log.debug("handled by Expense Delete Handler");
        return messageText.toLowerCase().startsWith("delete expense");
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            String[] parts = messageText.split(" ");
            if (parts.length < 3) {
                return "Format galat!\nSahi: delete expense 1";
            }
            int index = Integer.parseInt(parts[2])-1;
            if (expenseService.deleteExpenseByIndex(index)) {
                return "Expense deleted";
            } else {
                return "given index doesnt exist. Type /expenses to see list of expenses";
            }

        } catch (Exception e) {
            return "Number sahi dalo . eg. delete expense 1";
        }
    }
}
