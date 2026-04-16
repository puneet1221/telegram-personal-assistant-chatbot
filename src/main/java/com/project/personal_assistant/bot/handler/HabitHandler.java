package com.project.personal_assistant.bot.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.project.personal_assistant.model.Habit;
import com.project.personal_assistant.service.HabitService;

import lombok.extern.slf4j.Slf4j;

@Order(6)
@Component
@Slf4j
public class HabitHandler implements MessageHandler {
    @Autowired
    private HabitService habitService;

    @Override
    public boolean canHandle(String messageText, Long chatId) {
        log.info("{} handled by Habit Handler", messageText);
        return messageText.toLowerCase().contains("habit")
                || messageText.toLowerCase().startsWith("/today")
                || messageText.toLowerCase().startsWith("/mark-habit-done");
    }

    @Override
    public String handle(Update update, String messageText) {
        try {
            long chatId = update.getMessage().getFrom().getId();
            String lowerMessage = messageText.toLowerCase();

            if (messageText.startsWith("/add-habit")) {
                return handleAddHabit(messageText, chatId);

            } else if (lowerMessage.startsWith("/habits")) {
                return handleListHabits(chatId);

            } else if (lowerMessage.startsWith("/delete-habit")) {
                return handleDeleteHabit(messageText, chatId);

            } else if (lowerMessage.startsWith("/today")) {
                return handleTodayStatus(chatId);

            } else if (lowerMessage.startsWith("/mark-habit-done")) {
                return handleMarkHabitDone(messageText, chatId);

            }
            return "Sorry, I didn't understand that command.\n\n" +
                    "Available commands:\n" +
                    "/add-habit [name] - Add a new habit\n" +
                    "/habits - List all your habits\n" +
                    "/delete-habit [index] - Delete a habit\n" +
                    "/today - See today's habit status\n" +
                    "/mark-habit-done [index] - Mark habit as completed";

        } catch (NumberFormatException e) 
        {
            log.error("Number format error in habit handler", e);
            return "❌ Invalid input. Please provide valid numbers or habit names.";
        } catch (StringIndexOutOfBoundsException e) {
            log.error("String parsing error in habit handler", e);
            return "❌ Invalid format. Please check your command syntax.";
        } catch (Exception e) {
            log.error("Unexpected error in habit handler", e);
            return "❌ An unexpected error occurred. Please try again later.";
        }
    }

    private String handleAddHabit(String messageText, long chatId) {
        String[] parts = messageText.trim().split(" ", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "❌ Invalid format.\n\nUsage: /add-habit [habit name]\n" +
                    "Example: /add-habit gym";
        }
        try {
            Habit habit = new Habit();
            habit.setName(parts[1].trim());
            habit.setChatId(chatId);
            habit.setActive(true);
            return habitService.addHabit(habit);
        } catch (Exception e) {
            log.error("Error adding habit for chatId: {}", chatId, e);
            return "❌ Failed to add habit. Please try again.";
        }
    }

    private String handleListHabits(long chatId) {
        try {
            List<Habit> habits = habitService.findAllHabits(chatId);
            if (habits.isEmpty()) {
                return "You have no active habits. Use /add-habit [name] to create one.";
            }
            StringBuilder response = new StringBuilder("📋 Your active habits:\n\n");
            for (int i = 0; i < habits.size(); i++) {
                response.append(i + 1).append(". ").append(habits.get(i).getName()).append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            log.error("Error listing habits for chatId: {}", chatId, e);
            return "❌ Failed to retrieve habits. Please try again.";
        }
    }

    private String handleDeleteHabit(String messageText, long chatId) {
        String[] parts = messageText.trim().split(" ");
        if (parts.length < 2) {
            return "❌ Invalid format.\n\nSteps:\n" +
                    "1️⃣ /habits - Get your habit list\n" +
                    "2️⃣ /delete-habit [index] - Delete by index\n\n" +
                    "Example: /delete-habit 1";
        }
        try {
            int index = Integer.parseInt(parts[1]);
            List<Habit> habits = habitService.findAllHabits(chatId);
            if (index < 1 || index > habits.size()) {
                return "❌ Invalid index. Use /habits to see valid indices (1-" + habits.size() + ").";
            }
            Habit habitToDelete = habits.get(index - 1);
            if (habitService.deleteHabit(chatId, index)) {
                return "✅ \"" + habitToDelete.getName() + "\" has been deleted successfully.";
            } else {
                return "❌ Failed to delete the habit. Please try again.";
            }
        } catch (NumberFormatException e) {
            log.error("Invalid index format provided: {}", parts[1], e);
            return "❌ Please provide a valid number. Example: /delete-habit 1";
        } catch (Exception e) {
            log.error("Error deleting habit for chatId: {}", chatId, e);
            return "❌ Failed to delete habit. Please try again.";
        }
    }

    private String handleTodayStatus(long chatId) {
        try {
            return habitService.getTodayHabitStatus(chatId);
        } catch (Exception e) {
            log.error("Error retrieving today's habit status for chatId: {}", chatId, e);
            return "❌ Failed to retrieve today's status. Please try again.";
        }
    }

    private String handleMarkHabitDone(String messageText, long chatId) {
        String[] parts = messageText.trim().split(" ");
        if (parts.length < 2) {
            return "❌ Invalid format.\n\nUsage: /mark-habit-done [habit index]\n" +
                    "Example: /mark-habit-done 1\n\n" +
                    "Or use /today to see and update your habits.";
        }
        try {
            int index = Integer.parseInt(parts[1]);
            return habitService.logHabitCompletion(chatId, index);
        } catch (NumberFormatException e) {
            log.error("Invalid index format provided: {}", parts[1], e);
            return "❌ Please provide a valid number. Example: /mark-habit-done 1";
        } catch (Exception e) {
            log.error("Error logging habit completion for chatId: {}", chatId, e);
            return "❌ Failed to log habit completion. Please try again.";
        }
    }

}
