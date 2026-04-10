package com.project.personal_assistant.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.project.personal_assistant.model.User;

@Repository
public interface UserRepo extends MongoRepository<User, String> {
    boolean existsByChatId(Long chatId);
}