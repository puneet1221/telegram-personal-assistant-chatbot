package com.project.personal_assistant.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RateLimiterService {

    // Har user ka apna bucket — chatId → Bucket
    // 1 hour baad automatically expire hoga — memory waste nahi
    // maximumSize — 10000 users tak support karega
    private final Cache<Long, Bucket> userBuckets = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    // Har user ke liye bucket banao ya existing return karo
    private Bucket getBucketForUser(Long chatId) {
        return userBuckets.get(chatId,id -> createNewBucket()); //id yaha dummy h bas
    }

    // Naya bucket banao
    // 20 tokens — har minute mein refill hote hain
    // Matlab ek user 20 messages per minute bhej sakta hai
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Check karo — user allowed hai ya nahi
    // true = allowed, false = rate limit exceed
    public boolean isAllowed(Long chatId) {
        Bucket bucket = getBucketForUser(chatId);
        boolean allowed = bucket.tryConsume(1); 

        if (!allowed) {
            log.warn("Rate limit exceeded — chatId: {}, remaining: 0", chatId);
        } else {
            log.debug("Message allowed — chatId: {}, remaining tokens: {}",
                    chatId, bucket.getAvailableTokens());
        }

        return allowed;
    }

    public long getRemainingTokens(Long chatId) {
        return getBucketForUser(chatId)
                .getAvailableTokens();
    }
}