package com.project.personal_assistant.bot;

import com.google.gson.JsonObject;
import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.service.ExpenseService;
import com.project.personal_assistant.service.GeminiService;
import com.project.personal_assistant.service.QuartzService;
import com.project.personal_assistant.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class PersonalAssistantBot extends TelegramLongPollingBot {

    private final ExpenseService expenseService;
    private final ReminderService reminderService;
    private final QuartzService quartzService;
    private final GeminiService geminiService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public PersonalAssistantBot(ExpenseService expenseService,
            ReminderService reminderService,
            QuartzService quartzService,
            GeminiService geminiService,
            @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.expenseService = expenseService;
        this.reminderService = reminderService;
        this.quartzService = quartzService;
        this.geminiService = geminiService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/start")) {
            sendMessage(chatId,
                "Hello darling! 💕\n\n" +
                "Main yahan hoon aapki help karne ke liye — batao kya chahiye?\n\n" +
                "Examples:\n" +
                "aaj 500 khane pe kharch kiye\n" +
                "kal subah 8 baje gym yaad dilana\n" +
                "📋 /expenses — sab expenses\n" +
                "⏰ /reminders — sab reminders");

        } else if (messageText.startsWith("/expenses")) {
            handleGetExpenses(chatId);

        } else if (messageText.startsWith("/reminders")) {
            handleGetReminders(chatId);

        } else {
            handleNaturalLanguage(chatId, messageText);
        }
    }

    private void handleNaturalLanguage(long chatId, String message) {
        sendMessage(chatId, "Samajh rahi hoon... 💭");

        JsonObject result = geminiService.parseUserMessage(message);
        String type = result.get("type").getAsString();

        switch (type) {
            case "expense" -> handleExpenseFromAI(chatId, result);
            case "reminder" -> handleReminderFromAI(chatId, result);
            default -> {
                String reply = result.has("reply")
                    ? result.get("reply").getAsString()
                    : "Samjhi nahi yaar, thoda aur clear batao na! 😊";
                sendMessage(chatId, reply);
            }
        }
    }

    private void handleExpenseFromAI(long chatId, JsonObject data) {
        try {
            Double amount = data.get("amount").getAsDouble();
            String category = data.get("category").getAsString();
            String description = data.has("description")
                ? data.get("description").getAsString() : "";

            Expense expense = new Expense();
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDescription(description);
            expenseService.addExpense(expense);

            sendMessage(chatId,
                "Expense save ho gayi! ✅\n" +
                "Amount: Rs." + amount + "\n" +
                "Category: " + category + "\n" +
                "Description: " + description + "\n\n" +
                "Good job tracking your expenses, love! 💰");

        } catch (Exception e) {
            sendMessage(chatId, "Expense save nahi hui, dobara try karo na! 😔");
            log.error("Expense AI error: ", e);
        }
    }

    private void handleReminderFromAI(long chatId, JsonObject data) {
        try {
            String datetimeStr = data.get("datetime").getAsString();
            String message = data.get("message").getAsString();
            LocalDateTime reminderTime = LocalDateTime.parse(datetimeStr);

            Reminder saved = reminderService.addReminder(chatId, message, reminderTime);
            quartzService.scheduleReminder(saved.getId(), chatId, message, reminderTime);

            sendMessage(chatId,
                "Reminder set ho gayi! ⏰\n" +
                "Time: " + reminderTime + "\n" +
                "Message: " + message + "\n\n" +
                "Main yaad dila dungi, promise! 💕");

        } catch (Exception e) {
            sendMessage(chatId, "Reminder set nahi hui, dobara try karo na! 😔");
            log.error("Reminder AI error: ", e);
        }
    }

    private void handleGetExpenses(long chatId) {
        var expenses = expenseService.getAllExpenses();
        if (expenses.isEmpty()) {
            sendMessage(chatId, "Koi expense nahi abhi tak, darling! Start karte hain? 💸");
            return;
        }
        StringBuilder sb = new StringBuilder("Aapki expenses, jaaneman:\n\n");
        double total = 0;
        for (var expense : expenses) {
            sb.append("Rs.").append(expense.getAmount())
              .append(" — ").append(expense.getCategory())
              .append(" (").append(expense.getDescription()).append(")\n");
            total += expense.getAmount();
        }
        sb.append("\nTotal: Rs.").append(total).append(" 💰");
        sendMessage(chatId, sb.toString());
    }

    private void handleGetReminders(long chatId) {
        var reminders = reminderService.getAllReminders(chatId);
        if (reminders.isEmpty()) {
            sendMessage(chatId, "Koi reminder nahi abhi tak, sweetie! Set karte hain ek? ⏰");
            return;
        }
        StringBuilder sb = new StringBuilder("Aapke reminders, love:\n\n");
        for (var reminder : reminders) {
            sb.append("⏰ ").append(reminder.getReminderTime())
              .append(" — ").append(reminder.getMessage())
              .append(reminder.isSent() ? " (sent)" : " (pending)")
              .append("\n");
        }
        sendMessage(chatId, sb.toString());
    }

    public void sendReminderMessage(long chatId, String text) {
        sendMessage(chatId, text);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message send nahi hua: ", e);
        }
    }
}