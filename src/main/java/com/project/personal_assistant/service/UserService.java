package com.project.personal_assistant.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.User;
import com.project.personal_assistant.repo.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
@RequiredArgs Constructr: create constructor with all final and @NotNull fields as params.
@AllArgsConstructor: create constructor with all fields as params.
*/

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;
    private final MongoTemplate mongoTemplate;

    public List<Long> getAllActiveChatIds() {
        return mongoTemplate.getCollection("user")
                .distinct("chatId", Long.class)
                .into(new ArrayList<>());
    }

    public void registerUser(Long chatId) {
        if (!userRepository.existsByChatId(chatId)) {
            User user = new User();
            user.setChatId(chatId);
            userRepository.save(user);
            log.info("New user registered: {}", chatId);
        }
    }
}