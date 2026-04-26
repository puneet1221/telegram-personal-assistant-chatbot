package com.project.personal_assistant.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring service — automatically bean banega, inject ho sakta hai
@RequiredArgsConstructor
@Service
@Slf4j
public class SessionManagerService {

    private final VectorStore vectorStore;

    // Enum — user ki possible states define karta hai
    // NORMAL = sab handlers kaam karein
    // WAITING_FOR_FILE = bot file ka wait kar raha hai
    // QNA_SESSION = sirf QNA handler kaam karega
    public enum UserState {
        NORMAL,
        WAITING_FOR_FILE,
        WAITING_FOR_EDIT_EXPENSE,
        WAITING_FOR_DELETE_EXPENSE,
        WAITING_FOR_ADD_HABIT,
        WAITING_FOR_CITY,
        WAITING_FOR_ADD_REMINDER,
        QNA_SESSION
    }

    private final Map<Long, Integer> pendingIndex = new ConcurrentHashMap<>();

    public void setPendingIndex(Long chatId, int index) {
        pendingIndex.put(chatId, index);
    }

    public int getPendingIndex(Long chatId) {
        return pendingIndex.getOrDefault(chatId, -1);
    }

    // ConcurrentHashMap — thread safe hai
    // Multiple users ek saath bot use kar sakte hain
    // userId → UserState mapping store karta hai
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    // userId → documentId mapping
    // Ye track karta hai ki konsa CHUNKS konse user ne upload kiya
    private final Map<Long, List<String>> userDocuments = new ConcurrentHashMap<>();

    // User ki current state return karta hai
    // Agar state nahi mili toh default NORMAL return karo
    public UserState getState(Long userId) {
        return userStates.getOrDefault(userId, UserState.NORMAL);
    }

    // User ki state update karo
    // Jab /QNA:read-a-file aaye → WAITING_FOR_FILE
    // Jab file upload ho → QNA_SESSION
    // Jab /done aaye → NORMAL
    public void setState(Long userId, UserState state) {
        userStates.put(userId, state);
    }

    // User ka document ID store karo
    // Ye ID RAG backend ko bhejenge query ke time
    public void setDocument(Long userId, List<String> chunkIds) {
        userDocuments.put(userId, chunkIds);
    }

    // User ka current document ID lo
    // QNA_SESSION mein question aaye toh ye ID use hogi
    public List<String> getDocument(Long userId) {
        return userDocuments.get(userId);
    }

    // Session completely clear karo
    // /done command pe call hoga
    // State bhi jaayegi, document ID bhi jaayegi
    public void clearSession(Long userId) {

        List<String> chunkIdsToDelete = userDocuments.get(userId);
        if (chunkIdsToDelete != null && !chunkIdsToDelete.isEmpty()) {
            try {
                vectorStore.delete(chunkIdsToDelete);
                log.info("Deleted {} chunks from MongoDB for user {}", chunkIdsToDelete, userId);
            } catch (Exception e) {
                log.error("Failed to delete docs from VectorStore: {} for userId {}", e.getMessage(), userId);
            }
        }
        userStates.remove(userId);
        userDocuments.remove(userId);
    }
}