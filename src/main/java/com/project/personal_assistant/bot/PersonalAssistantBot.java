package com.project.personal_assistant.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.service.ExpenseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PersonalAssistantBot extends TelegramLongPollingBot {
    private final ExpenseService expenseService;
    @Value("${telegram.bot.username}")
    private String botUsername;

    public PersonalAssistantBot(ExpenseService expenseService,
            @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.expenseService = expenseService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String messageText = update.getMessage().getText().toLowerCase().trim();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/start")) {
            sendMessage(chatId, "Dogesh Bhai hazir h aapki seva me 🙏\n\n\n" +
                    "Commands:\n" +
                    "💰 expense <amount> <category> <description>\n" +
                    "📋 /expenses - sab expenses dekho\n" +
                    "❓ /help - help");

        } else if (messageText.startsWith("expense")) {
            handleExpense(chatId, messageText);

        } else if (messageText.startsWith("/expenses")) {
            handleGetExpenses(chatId);

        } else {
            sendMessage(chatId, "Samjha nahi bhai 😅 /help try karo");
        }
    }

    private void handleExpense(long chatId, String message) {
        try {
            // Format: expense 500 food lunch
            String[] parts = message.split(" ", 4);
            if (parts.length < 3) {
                sendMessage(chatId, " Format galat hai!\nSahi format: expense 500 food lunch");
                return;
            }

            Double amount = Double.parseDouble(parts[1]);
            String category = parts[2];
            String description = parts.length == 4 ? parts[3] : "";

            Expense expense = new Expense();
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDescription(description);

            expenseService.addExpense(expense);

            sendMessage(chatId, "✅ Expense save ho gaya!\n" +
                    "Amount: ₹" + amount + "\n" +
                    "Category: " + category + "\n" +
                    "Description: " + description);

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Amount number mein daalo bhai!\nExample: expense 500 food lunch");
        }
    }

    private void handleGetExpenses(long chatId) {
        var expenses = expenseService.getAllExpenses();
        if (expenses.isEmpty()) {
            sendMessage(chatId, "Koi expense nahi abhi tak!");
            return;
        }

        StringBuilder sb = new StringBuilder("📋 Teri expenses:\n\n");
        double total = 0;
        for (var expense : expenses) {
            sb.append("• ₹").append(expense.getAmount())
                    .append(" - ").append(expense.getCategory())
                    .append(" (").append(expense.getDescription()).append(")\n");
            total += expense.getAmount();
        }
        sb.append("\n💰 Total: ₹").append(total);
        sendMessage(chatId, sb.toString());
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error in sending the message ", e);
        }
    }
}