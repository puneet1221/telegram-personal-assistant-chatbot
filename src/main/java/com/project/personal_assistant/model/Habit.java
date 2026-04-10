package com.project.personal_assistant.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "habits")
public class Habit {
    @Id
    private String id;
    private Long chatId;
    private String name;
    private boolean active = false;
}
