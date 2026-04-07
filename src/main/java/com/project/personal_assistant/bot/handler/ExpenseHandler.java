package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.google.gson.JsonObject;
import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.service.ExpenseService;
import com.project.personal_assistant.service.GeminiService;

import lombok.RequiredArgsConstructor;

@Component
@Order(6)
@RequiredArgsConstructor
public class ExpenseHandler implements MessageHandler {

    private final ExpenseService expenseService;
    private final GeminiService geminiService;

    @Override
    public boolean canHandle(String messageText) {
       if (messageText.toLowerCase().startsWith("delete") ||
        messageText.toLowerCase().startsWith("edit") ||
        messageText.toLowerCase().startsWith("/")) {
        return false;
    }

        JsonObject parsed = geminiService.parseUserMessage(messageText);
        return "expense".equals(parsed.get("type").getAsString());
    }

    @Override
    public String handle(Update update, String messageText) {
        try {

            JsonObject data = geminiService.parseUserMessage(messageText);

            Double amount = data.get("amount").getAsDouble();
            String category = data.get("category").getAsString();
            String description = data.has("description")
                    ? data.get("description").getAsString()
                    : "";
            
                    //dto
            Expense expense = new Expense();
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDescription(description);

            //pushing to db
            expenseService.addExpense(expense);

            return "Expense save ho gaya!\n" +
                    "Amount: Rs." + amount + "\n" +
                    "Category: " + category + "\n" +
                    "Description: " + description;

        } catch (Exception e) {
            return "Expense save nahi hua, dobara try karo!";
        }
    }
}