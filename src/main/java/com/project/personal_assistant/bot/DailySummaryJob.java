package com.project.personal_assistant.bot;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.project.personal_assistant.service.DailySummaryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DailySummaryJob implements Job {

    private DailySummaryService dailySummaryService;
    private PersonalAssistantBot assistantBot;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Daily Summary Job staretd");
        dailySummaryService.sendSummaryToAllUsers(assistantBot);
        log.info("daily summary Job completed");
    }

}
