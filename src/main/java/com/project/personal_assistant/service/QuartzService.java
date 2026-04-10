package com.project.personal_assistant.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.project.personal_assistant.bot.DailySummaryJob;
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

    public void scheduleRecurringReminder(String reminderId, Long chatId,
            String message, String cronExpression) {
        try {
            JobDetail job = JobBuilder.newJob(ReminderJob.class)
                    .withIdentity("recurring-" + reminderId)
                    .usingJobData("reminderId", reminderId)
                    .usingJobData("chatId", chatId)
                    .usingJobData("message", message)
                    .usingJobData("recurring", true)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("recurring-trigger-" + reminderId)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.info("Recurring reminder scheduled: {} with cron: {}", message, cronExpression);

        } catch (SchedulerException e) {
            log.error("Recurring reminder schedule nahi hua: ", e);
        }
    }

    public void scheduleDailySummary() {
        try {
            JobDetail job = JobBuilder.newJob(DailySummaryJob.class)
                    .withIdentity("daily-summary-job")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("daily-summary-trigger")
                    .withSchedule(CronScheduleBuilder
                            .cronSchedule("0 0 22 * * ?")
                            .inTimeZone(TimeZone.getTimeZone("Asia/Kolkata")))
                    .build();

            if (!scheduler.checkExists(new JobKey("daily-summary-job"))) {
                scheduler.scheduleJob(job, trigger);
                log.info("Daily summary scheduled at 10 PM IST");
            }

        } catch (SchedulerException e) {
            log.error("Daily summary schedule nahi hua: ", e);
        }
    }
}