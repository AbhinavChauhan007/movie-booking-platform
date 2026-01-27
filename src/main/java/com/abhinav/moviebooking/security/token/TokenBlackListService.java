package com.abhinav.moviebooking.security.token;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlackListService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blackList(String token, long expiryTimeMillis) {
        long ttlSeconds = (expiryTimeMillis - System.currentTimeMillis()) / 1000;
        if (ttlSeconds <= 0) return;
        redisTemplate.opsForValue().set(token, "BLACKLISTED", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlackListed(String token) {
        return redisTemplate.hasKey(token);

    }
}
