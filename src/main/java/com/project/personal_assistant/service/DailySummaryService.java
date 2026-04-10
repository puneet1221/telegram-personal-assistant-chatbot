package com.project.personal_assistant.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.personal_assistant.bot.PersonalAssistantBot;
import com.project.personal_assistant.model.Expense;
import com.project.personal_assistant.model.Reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySummaryService {

    private final ExpenseService expenseService;
    private final HabitService habitService;
    private final ReminderService reminderService;
    private final UserService userService;

    public void sendSummaryToAllUsers(PersonalAssistantBot bot) {
        List<Long> allUsers = userService.getAllActiveChatIds();
        for (Long chatId : allUsers) {
            try {
                String summary = buildSummary(chatId);
                bot.sendReminderMessage(chatId, summary);
                log.info("Summary sent to: {}", chatId);
            } catch (Exception e) {
                log.error("Summary send nahi hua for chatId: {}", chatId, e);
            }
        }
    }

    public String buildSummary(Long chatId) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Aaj ka summary — ")
                .append(LocalDate.now()).append("\n\n");

        // Expenses
        List<Expense> expenses = expenseService.getTodayExpenses(chatId);
        if (!expenses.isEmpty()) {
            double total = expenses.stream()
                    .mapToDouble(Expense::getAmount).sum();
            sb.append("💰 Expenses: Rs.").append(total).append("\n");

            Map<String, Double> byCategory = expenses.stream()
                    .collect(Collectors.groupingBy(
                            Expense::getCategory,
                            Collectors.summingDouble(Expense::getAmount)));

            byCategory.forEach((cat, amt) -> sb.append("   • ").append(cat)
                    .append(": Rs.").append(amt).append("\n"));
        } else {
            sb.append("💰 Expenses: Koi expense nahi aaj\n");
        }

        sb.append("\n");

        // Habits
        String habitStatus = habitService.getTodayHabitStatus(chatId);
        sb.append("✅ Habits:\n").append(habitStatus).append("\n");

        // Kal ke reminders
        LocalDateTime tomorrowStart = LocalDateTime.of(
                LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        LocalDateTime tomorrowEnd = LocalDateTime.of(
                LocalDate.now().plusDays(1), LocalTime.MAX);

        List<Reminder> tomorrowReminders = reminderService
                .getRemindersBetween(chatId, tomorrowStart, tomorrowEnd);

        if (!tomorrowReminders.isEmpty()) {
            sb.append("⏰ Kal ke reminders:\n");
            tomorrowReminders.forEach(r -> sb.append("   • ").append(r.getReminderTime().toLocalTime())
                    .append(" — ").append(r.getMessage()).append("\n"));
        } else {
            sb.append("⏰ Kal ke reminders: Koi nahi\n");
        }

        return sb.toString();
    }
}