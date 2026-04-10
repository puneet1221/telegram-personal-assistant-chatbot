package com.project.personal_assistant.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.Habit;
import com.project.personal_assistant.model.HabitLog;
import com.project.personal_assistant.repo.HabitLogRepository;
import com.project.personal_assistant.repo.HabitRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    public String addHabit(Habit habit) {
        habitRepository.save(habit);
        return "\n" + habit.getName() + " added successfully to your list of habits";
    }

    public List<HabitLog> getTodayStatus(Long chatId) {
        return habitLogRepository.findByChatIdAndDate(chatId, LocalDate.now());
    }

    public List<Habit> findAllHabits(Long chatId) {
        return habitRepository.findByChatIdAndActive(chatId, Boolean.TRUE);
    }

    public boolean deleteHabit(Long chatId, int index) {
        List<Habit> habits = findAllHabits(chatId);
        if (index < 1 || index > habits.size()) {
            return false;
        }
        Habit habit = habits.get(index - 1);
        habit.setActive(false);
        habitRepository.save(habit);
        return true;
    }

    public String logHabitCompletion(Long chatId, int index) {

        try {
            List<Habit> habits = habitRepository.findByChatIdAndActive(chatId, Boolean.TRUE);
            if (index < 1 || index > habits.size()) {
                log.warn("Invalid habit index: {} for chatId: {}", index, chatId);
                return "❌ Invalid habit index. Use /habits to see valid indices (1-" + habits.size() + ").";
            }

            Habit habit = habits.get(index - 1);

            // Duplicate check
            boolean alreadyLogged = habitLogRepository
                    .findByChatIdAndDate(chatId, LocalDate.now())
                    .stream()
                    .anyMatch(l -> l.getHabitId().equals(habit.getId()));

            if (alreadyLogged) {
                return "\"" + habit.getName() + "\" aaj already mark ho gaya hai!";
            }
            HabitLog log_entry = new HabitLog();
            log_entry.setChatId(chatId);
            log_entry.setHabitId(habit.getId());
            log_entry.setHabitName(habit.getName());
            log_entry.setDate(LocalDate.now());
            habitLogRepository.save(log_entry);

            log.info("Habit logged: {} (index: {}) for chatId: {}", habit.getName(), index, chatId);
            return "✅ Great! \"" + habit.getName() + "\" logged for today.";
        } catch (Exception e) {
            log.error("Error logging habit completion for chatId: {}", chatId, e);
            return "❌ Failed to log habit completion. Please try again.";
        }
    }

    public String getTodayHabitStatus(Long chatId) {
        try {
            List<HabitLog> todayLogs = getTodayStatus(chatId);
            List<Habit> allHabits = findAllHabits(chatId);

            if (allHabits.isEmpty()) {
                return "You have no active habits. Use /add-habit [name] to create one.";
            }

            StringBuilder response = new StringBuilder("📊 Today's Habit Status:\n\n");
            for (int i = 0; i < allHabits.size(); i++) {
                Habit habit = allHabits.get(i);
                boolean completed = todayLogs.stream()
                        .anyMatch(log -> log.getHabitId().equals(habit.getId()));
                String status = completed ? "✅" : "❌";
                response.append((i + 1) + "- ").append(status).append(" ").append(habit.getName()).append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            log.error("Error fetching habit status for chatId: {}", chatId, e);
            return "Failed to retrieve today's habit status. Please try again.";
        }
    }

}
