package com.project.personal_assistant.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection="Reminders")
public class Reminder{
 private String id;
 private Long chatId;
 private String message;
 private LocalDateTime reminderTime;
 private boolean sent=false;
 
 private boolean recurring=false;
 private String frequency;
 private String dayOfWeek;
 private String timeOfDay;
 private String cronExpression;
}
