package com.project.personal_assistant.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.bot.PersonalAssistantBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final PersonalAssistantBot bot;

    @PostMapping("/webhook")
    public void onUpdateReceived(@RequestBody Update update) {
        log.info("Webhook update received");
        bot.onWebhookUpdateReceived(update);
    }
}