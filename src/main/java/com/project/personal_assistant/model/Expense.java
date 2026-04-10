package com.project.personal_assistant.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    private Long chatId;
    private String category;
    private Double amount;
    private String description;
    private LocalDateTime date = LocalDateTime.now();
}