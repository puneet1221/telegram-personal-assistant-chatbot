package com.project.personal_assistant.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageHandler {
    boolean canHandle(String messageText,Long chatId);

    String handle(Update update, String messageText);
}

/*
 * Ye kyun banaya — samajh:
 * canHandle() — har handler khud decide karta hai ki ye message mera hai ya
 * nahi. Bot ko nahi pata hoga andar ki logic — sirf poochhega
 * "kya tu handle kar sakta hai?"
 * handle() — agar haan, toh karo aur response string return karo.
 * Faida: Kal tu weather handler, notes handler kuch bhi add kare — sirf naya
 * class banao jo ye interface implement kare. PersonalAssistantBot.java mein ek
 * line bhi nahi badlegi.
 * 
 * Ye Open/Closed Principle hai — open for extension, closed for modification.
 * Interviews mein SOLID principles ke naam se poochha jaata hai. 🙏
 * 
 * 
 */