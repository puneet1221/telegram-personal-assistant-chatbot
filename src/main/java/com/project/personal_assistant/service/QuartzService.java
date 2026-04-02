package com.project.personal_assistant.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.project.personal_assistant.bot.ReminderJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzService {

    private final Scheduler scheduler;

    public void scheduleReminder(String reminderId, Long chatId,
            String message, LocalDateTime reminderTime) {
        try {
            JobDetail job = JobBuilder.newJob(ReminderJob.class)
                    .withIdentity("reminder-" + reminderId)
                    .usingJobData("reminderId", reminderId)
                    .usingJobData("chatId", chatId)
                    .usingJobData("message", message)
                    .build();

            Date triggerTime = Date.from(
                    reminderTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + reminderId)
                    .startAt(triggerTime)
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.info("Reminder scheduled: {} at {}", message, reminderTime);

        } catch (SchedulerException e) {
            log.error("Reminder schedule nahi hua: ", e);
        }
    }
}