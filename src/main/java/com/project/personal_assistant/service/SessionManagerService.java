package com.project.personal_assistant.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// Spring service — automatically bean banega, inject ho sakta hai
@Service
public class SessionManagerService {

    
    // Enum — user ki possible states define karta hai
    // NORMAL = sab handlers kaam karein
    // WAITING_FOR_FILE = bot file ka wait kar raha hai
    // QNA_SESSION = sirf QNA handler kaam karega
    public enum UserState {
        NORMAL,
        WAITING_FOR_FILE,
        QNA_SESSION
    }

    // ConcurrentHashMap — thread safe hai
    // Multiple users ek saath bot use kar sakte hain
    // chatId → UserState mapping store karta hai
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    // chatId → documentId mapping
    // Ye track karta hai ki konsa document konse user ne upload kiya
    private final Map<Long, String> userDocuments = new ConcurrentHashMap<>();

    // User ki current state return karta hai
    // Agar state nahi mili toh default NORMAL return karo
    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NORMAL);
    }

    // User ki state update karo
    // Jab /QNA:read-a-file aaye → WAITING_FOR_FILE
    // Jab file upload ho → QNA_SESSION
    // Jab /done aaye → NORMAL
    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    // User ka document ID store karo
    // Ye ID RAG backend ko bhejenge query ke time
    public void setDocument(Long chatId, String documentId) {
        userDocuments.put(chatId, documentId);
    }

    // User ka current document ID lo
    // QNA_SESSION mein question aaye toh ye ID use hogi
    public String getDocument(Long chatId) {
        return userDocuments.get(chatId);
    }

    // Session completely clear karo
    // /done command pe call hoga
    // State bhi jaayegi, document ID bhi jaayegi
    public void clearSession(Long chatId) {
        userStates.remove(chatId);
        userDocuments.remove(chatId);
    }
}