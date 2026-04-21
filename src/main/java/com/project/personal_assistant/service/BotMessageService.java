package com.project.personal_assistant.service;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface BotMessageService {

    public void sendMainMenu(Long chatId);

    public void sendInlineKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> buttons);

    public void sendMessage(long chatId, String text);

    public void sendAction(ActionType action, Long chatId);
}
