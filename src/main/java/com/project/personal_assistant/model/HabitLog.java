package com.project.personal_assistant.model;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "habit_logs")
public class HabitLog {
    @Id
    private String id;
    private Long chatId;
    private String habitId;
    private String habitName;
    private LocalDate date;
}
