package com.project.personal_assistant.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.personal_assistant.model.User;
import com.project.personal_assistant.repo.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepository;

    public void registerUser(Long chatId) {
        if (!userRepository.existsByChatId(chatId)) {
            User user = new User();
            user.setChatId(chatId);
            userRepository.save(user);
            log.info("New user registered: {}", chatId);
        }
    }

    public List<Long> getAllActiveChatIds() {
        return userRepository.findAll()
                .stream()
                .map(User::getChatId)
                .collect(Collectors.toList());
    }
}