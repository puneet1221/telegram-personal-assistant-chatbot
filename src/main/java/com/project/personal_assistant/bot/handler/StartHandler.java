package com.project.personal_assistant.bot.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;

@Order(1)
@RequiredArgsConstructor
@Component
public class StartHandler implements MessageHandler {

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        return messageText.startsWith("/start");
    }

    @Override
    public String handle(Update update, String messageText) {
        String firstName = update.getMessage().getFrom().getFirstName();

        return """
                *Hello %s!* 👋

                Main hu tumhara personal assistant, *Dogesh*! 🐕
                Main tumhari life organize karne mein help karunga.

                *Main kya-kya kar sakta hu?*
                💰 *Expense Tracking* - Bas apna kharcha likho.
                ⏰ *Reminders* - Kuch bhulne nahi dunga.
                📅 *Habit Tracking* - Daily goals poore karo.
                📄 *PDF Reader* - Documents mujhse discuss karo.
                ☁️ *Weather & News* - Daily updates pao.
                📝 *Daily Summary* - Din bhar ka hisab raat ko.

                Batao bhai, kis cheez se shuru karein?
                """.formatted(firstName);
    }
}