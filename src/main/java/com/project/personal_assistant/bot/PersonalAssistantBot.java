package com.project.personal_assistant.bot;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.project.personal_assistant.bot.handler.FileUploadHandler;
import com.project.personal_assistant.bot.handler.MessageHandler;
import com.project.personal_assistant.service.GroqChatService;
import com.project.personal_assistant.service.SessionManagerService;
import com.project.personal_assistant.service.SessionManagerService.UserState;
import com.project.personal_assistant.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PersonalAssistantBot extends TelegramWebhookBot {

    private final List<MessageHandler> handlers;
    private final GroqChatService groqChatService;
    private final UserService userService;
    private final SessionManagerService sessionManager;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${webhook.url}")
    private String webhookUrl;

    public PersonalAssistantBot(
            List<MessageHandler> handlers,
            GroqChatService groqChatService,
            UserService userService,
            SessionManagerService sessionManager,
            FileUploadHandler fileUploadHandler,
            @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.handlers = handlers;
        this.groqChatService = groqChatService;
        this.userService = userService;
        this.sessionManager = sessionManager;
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
        if (!update.hasMessage())
            return null;
        long chatId = update.getMessage().getChatId();
        if (update.getMessage().hasPhoto() || update.getMessage().hasVoice() || update.getMessage().hasAudio()) {
            String name = update.getMessage().getFrom().getFirstName();
            sendMessage(chatId,
                    String.format("Sorry Dear %s <3 I can't handle these yet. Currently I can handle text only", name));
            return null;
        }

        log.info("received update {}", update.getMessage());

        // User register karo — pehli baar aaya toh save karo
        userService.registerUser(chatId);

        // File upload check — WAITING_FOR_FILE state mein
        if (update.getMessage().hasDocument()) {
            log.info("Document received from user {} \n\n {}", update.getMessage().getChatId(), update);

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

    public void sendReminderMessage(long chatId, String text) {
        sendMessage(chatId, text);
    }

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