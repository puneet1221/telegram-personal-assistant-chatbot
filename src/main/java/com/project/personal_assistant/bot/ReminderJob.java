package com.project.personal_assistant.bot;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.project.personal_assistant.service.ReminderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReminderJob implements Job {

    @Autowired
    private PersonalAssistantBot bot;

    @Autowired
    private ReminderService reminderService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String reminderId = data.getString("reminderId");
        String message = data.getString("message");
        Long chatId = data.getLong("chatId");

        bot.sendReminderMessage(chatId, "Reminder: " + message);
        reminderService.markSentById(reminderId);
        log.info("Reminder fired: {}", message);
    }
}