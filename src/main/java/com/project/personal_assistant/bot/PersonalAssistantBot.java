package com.project.personal_assistant.bot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.personal_assistant.bot.handler.ExpensesListHandler;
import com.project.personal_assistant.bot.handler.MessageHandler;
import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.model.Reminder;
import com.project.personal_assistant.service.BotMessageService;
import com.project.personal_assistant.service.ExpenseService;
import com.project.personal_assistant.service.GroqChatService;
import com.project.personal_assistant.service.ReminderService;
import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;
import com.project.personal_assistant.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PersonalAssistantBot extends TelegramWebhookBot implements BotMessageService {
    private final List<MessageHandler> handlers;
    private final GroqChatService groqChatService;
    private final UserService userService;
    private final SessionManagerService sessionManager;
    private final ExpenseService expenseService;
    private final ReminderService reminderService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${webhook.url}")
    private String webhookUrl;

    public PersonalAssistantBot(
            List<MessageHandler> handlers,
            GroqChatService groqChatService,
            ReminderService reminderService,
            UserService userService,
            SessionManagerService sessionManager,
            ExpenseService expenseService,
            @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.handlers = handlers;
        this.groqChatService = groqChatService;
        this.userService = userService;
        this.sessionManager = sessionManager;
        this.expenseService = expenseService;
        this.reminderService = reminderService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return webhookUrl + "/webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            log.info("call back query received");
            handleCallback(update);
            return null;
        }

        if (!update.hasMessage())
            return null;
        long chatId = update.getMessage().getChatId();
        if (update.getMessage().hasPhoto() || update.getMessage().hasVoice() || update.getMessage().hasAudio()) {
            String name = update.getMessage().getFrom().getFirstName();
            sendMessage(chatId,
                    String.format("Sorry Dear %s <3 I can't handle these yet. Currently I can handle text only", name));
            return null;
        }

        // User register karo — pehli baar aaya toh save karo
        userService.registerUser(chatId);

        // File upload check — WAITING_FOR_FILE state mein
        if (update.getMessage().hasDocument()) {
            log.info("Document received from user {} \n\n {}", update.getMessage().getChatId(), update);
            sendAction(ActionType.UPLOADDOCUMENT, chatId);
            UserState state = sessionManager.getState(chatId);

            if (state == UserState.WAITING_FOR_FILE) {
                // ✅ handlers list se extract karo — directly fileUploadHandler mat bulao
                String response = handlers.stream()
                        .filter(h -> h.canHandle("", chatId)) // state se match hoga
                        .findFirst()
                        .map(h -> h.handle(update, ""))
                        .orElse("Kuch galat hua, dobara try karo!");

                sendMessage(chatId, response);
            } else {
                sendMessage(chatId, "File receive hua!\n" +
                        "Document Q&A ke liye pehle /QNA:read-a-file likho.");
            }
            return null;
        }

        // Text message check
        if (!update.getMessage().hasText())
            return null;

        String messageText = update.getMessage().getText().trim();
        if (messageText.toLowerCase().equals("/start")) {
            sendMainMenu(chatId);
            return null;
        }
        if (messageText.equals("💰 Expenses")) {
            sendExpenseMenu(chatId);
            return null;
        }
        if (messageText.equals("⏰ Reminders")) {
            sendReminderMenu(chatId);
            return null;
        }
        sendMessage(chatId, "Samajh raha hoon...");

        // Handler dhundho — chatId bhi pass karo session check ke liye
        String response = handlers.stream()
                .filter(h -> h.canHandle(messageText, chatId))
                .findFirst()
                .map(h -> h.handle(update, messageText))
                .orElse("Kuch galat hua, dobara try karo!");

        // Null response — kisi handler ne handle nahi kiya
        if (response != null) {
            groqChatService.clearCache(messageText);
            sendMessage(chatId, response);
        }

        return null;
    }

    public void sendReminderMenu(Long chatId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton add = new InlineKeyboardButton("➕ Add Reminder");
        add.setCallbackData("reminder:add");

        InlineKeyboardButton view = new InlineKeyboardButton("📋 View All");
        view.setCallbackData("reminder:view");

        InlineKeyboardButton delete = new InlineKeyboardButton("🗑️ Delete");
        delete.setCallbackData("reminder:delete");

        InlineKeyboardButton deletePast = new InlineKeyboardButton("🧹 Delete Past");
        deletePast.setCallbackData("reminder:delete:past");

        buttons.add(List.of(add, view));
        buttons.add(List.of(delete, deletePast));
        sendInlineKeyboard(chatId, "⏰ Reminder Manager", buttons);

    }

    public void sendReminderMessage(long chatId, String text) {
        sendMessage(chatId, text);
    }

    private void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callbackData.startsWith("expense:edit:")) {
            if (callbackData.equals("expense:edit:"))
                return;
            int index = Integer.parseInt(callbackData.split(":")[2]);
            sessionManager.setPendingIndex(chatId, index);
            sessionManager.setState(chatId, UserState.WAITING_FOR_EDIT_EXPENSE);
            sendMessage(chatId,
                    "✏️ Naya details batao:\n\n" +
                            "Format: <amount> <category> <description>\n" +
                            "Example: 400 food lunch");
            return;
        }

        if (callbackData.startsWith("expense:delete:")) {
            String action = callbackData.split(":")[2];

            if (action.equals("confirm")) {
                int index = sessionManager.getPendingIndex(chatId);
                boolean deleted = expenseService.deleteExpenseByIndex(index, chatId);
                sessionManager.setState(chatId, UserState.NORMAL);
                sendMessage(chatId, deleted ? "🗑️ Expense delete ho gaya!" : "❌ Expense nahi mila!");
                return;
            }

            if (action.equals("cancel")) {
                sessionManager.setState(chatId, UserState.NORMAL);
                sendMessage(chatId, "❌ Cancel kar diya!");
                return;
            }

            // specific index pe delete
            int index = Integer.parseInt(action);
            sessionManager.setPendingIndex(chatId, index);
            sessionManager.setState(chatId, UserState.WAITING_FOR_DELETE_EXPENSE);

            InlineKeyboardButton yes = new InlineKeyboardButton("✅ Haan Delete Karo");
            yes.setCallbackData("expense:delete:confirm");
            InlineKeyboardButton no = new InlineKeyboardButton("❌ Cancel");
            no.setCallbackData("expense:delete:cancel");
            sendInlineKeyboard(chatId, "🗑️ Confirm karo?", List.of(List.of(yes, no)));
            return;
        }

        if (callbackData.equals("reminder:delete:past")) {
            String response = reminderService.deleteAllPastReminders(chatId);
            sendMessage(chatId, response);
            return;
        }

        // reminder:delete:<index>
        if (callbackData.startsWith("reminder:delete:")) {
            String action = callbackData.split(":")[2];

            if (action.equals("confirm")) {
                int index = sessionManager.getPendingIndex(chatId);
                reminderService.deleteByIndex(index, chatId);
                sessionManager.setState(chatId, UserState.NORMAL);
                sendMessage(chatId, "🗑️ Reminder delete ho gaya!");
                return;
            }
            if (action.equals("cancel")) {
                sessionManager.setState(chatId, UserState.NORMAL);
                sendMessage(chatId, "❌ Cancel kar diya!");
                return;
            }

            // specific index
            int index = Integer.parseInt(action);
            sessionManager.setPendingIndex(chatId, index);

            InlineKeyboardButton yes = new InlineKeyboardButton("✅ Haan Delete Karo");
            yes.setCallbackData("reminder:delete:confirm");
            InlineKeyboardButton no = new InlineKeyboardButton("❌ Cancel");
            no.setCallbackData("reminder:delete:cancel");
            sendInlineKeyboard(chatId, "🗑️ Confirm?", List.of(List.of(yes, no)));
            return;
        }

        switch (callbackData) {
            case "expense:add" -> sendMessage(chatId,
                    "💰 Expense batao!\n\nExample: aaj 500 khane pe kharch kiya");

            case "expense:view" -> {
                Message message = update.getCallbackQuery().getMessage();
                message.setFrom(update.getCallbackQuery().getFrom());
                update.setMessage(message);
                String response = handlers.stream()
                        .filter(h -> h instanceof ExpensesListHandler)
                        .findFirst()
                        .map(h -> h.handle(update, "expense:view"))
                        .orElse("Kuch galat hua!");
                sendMessage(chatId, response);
            }

            case "expense:edit" -> {
                // list dikhao har item pe ✏️ button
                List<Expense> expenses = expenseService.getAllExpenses(chatId);
                if (expenses.isEmpty()) {
                    sendMessage(chatId, "Koi expense nahi abhi tak!");
                    return;
                }
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                for (int i = 0; i < expenses.size(); i++) {
                    Expense e = expenses.get(i);
                    InlineKeyboardButton btn = new InlineKeyboardButton(
                            (i + 1) + ". ₹" + e.getAmount() + " — " + e.getCategory() + " — " + e.getDescription());
                    btn.setCallbackData("expense:edit:" + i);
                    rows.add(List.of(btn));
                }
                sendInlineKeyboard(chatId, "✏️ Kaunsa expense edit karein?", rows);
            }

            case "expense:delete" -> {
                // ✅ list dikhao har item pe 🗑️ button — text nahi!
                List<Expense> expenses = expenseService.getAllExpenses(chatId);
                if (expenses.isEmpty()) {
                    sendMessage(chatId, "Koi expense nahi abhi tak!");
                    return;
                }
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                for (int i = 0; i < expenses.size(); i++) {
                    Expense e = expenses.get(i);
                    InlineKeyboardButton btn = new InlineKeyboardButton(
                            (i + 1) + ". ₹" + e.getAmount() + " — " + e.getCategory() + " — " + e.getDescription());
                    btn.setCallbackData("expense:delete:" + i);
                    rows.add(List.of(btn));
                }
                sendInlineKeyboard(chatId, "🗑️ Kaunsa expense delete karein?", rows);
            }

            case "reminder:add" -> sendMessage(chatId,
                    "⏰ Reminder batao!\n\nExample: kal subah 8 baje gym jaana hai");

            case "reminder:view" -> {
                Message message = update.getCallbackQuery().getMessage();
                message.setFrom(update.getCallbackQuery().getFrom());
                update.setMessage(message);

                List<Reminder> reminders = reminderService.getAllReminders(chatId);
                if (reminders.isEmpty()) {
                    sendMessage(chatId, "Koi reminder nahi abhi tak!");
                    return;
                }

                // har reminder pe delete button
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                for (int i = 0; i < reminders.size(); i++) {
                    Reminder r = reminders.get(i);
                    InlineKeyboardButton btn = new InlineKeyboardButton(
                            (i + 1) + ". " + r.getReminderTime() + " — " + r.getMessage()
                                    + (r.isSent() ? " ✅" : " ⏳"));
                    btn.setCallbackData("reminder:delete:" + i);
                    rows.add(List.of(btn));
                }
                sendInlineKeyboard(chatId, "⏰ Tere Reminders:", rows);
            }

            case "reminder:delete" -> {
                List<Reminder> reminders = reminderService.getAllReminders(chatId);
                if (reminders.isEmpty()) {
                    sendMessage(chatId, "Koi reminder nahi abhi tak!");
                    return;
                }
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                for (int i = 0; i < reminders.size(); i++) {
                    Reminder r = reminders.get(i);
                    InlineKeyboardButton btn = new InlineKeyboardButton(
                            (i + 1) + ". " + r.getReminderTime() + " — " + r.getMessage());
                    btn.setCallbackData("reminder:delete:" + i);
                    rows.add(List.of(btn));
                }
                sendInlineKeyboard(chatId, "🗑️ Kaunsa reminder delete karein?", rows);
            }
        }
    }

    @Override
    public void sendMainMenu(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("💰 Expenses");
        row1.add("⏰ Reminders");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📅 Habits");
        row2.add("📄 PDF Q&A");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("☁️ Weather");
        row3.add("📝 Summary");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Main Menu 👇");
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Menu send nahi hua: ", e);
        }

    }

    private void sendExpenseMenu(long chatId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Row 1
        InlineKeyboardButton add = new InlineKeyboardButton("➕ Add Expense");
        add.setCallbackData("expense:add");

        InlineKeyboardButton view = new InlineKeyboardButton("📋 View All");
        view.setCallbackData("expense:view");

        // Row 2
        InlineKeyboardButton edit = new InlineKeyboardButton("✏️ Edit");
        edit.setCallbackData("expense:edit");

        InlineKeyboardButton delete = new InlineKeyboardButton("🗑️ Delete");
        delete.setCallbackData("expense:delete");

        buttons.add(List.of(add, view));
        buttons.add(List.of(edit, delete));

        sendInlineKeyboard(chatId, "💰 Expense Manager", buttons);
    }

    @Override
    public void sendInlineKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> buttons) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(buttons);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Failed to Send Inline Keyboard", e.getMessage());
        }
    }

    @Override

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message send nahi hua: ", e);
        }
    }

    @Override
    public void sendAction(ActionType action, Long chatId) {
        SendChatAction chatAction = new SendChatAction();
        chatAction.setChatId(chatId.toString());
        chatAction.setAction(action);
        try {
            execute(chatAction);
        } catch (Exception e) {
            log.error("error sending tan Action...", e);
        }
    }
}