package com.abhinav.moviebooking.security.token;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {

    private final Map<String, Long> blackListedTokens = new ConcurrentHashMap<>();

    public void blackList(String token, long expiryTime) {
        System.out.println("====== before blackListing ======== " + blackListedTokens.toString());
        blackListedTokens.put(token, expiryTime);
        System.out.println("====== before blackListing ======== " + blackListedTokens.toString());
    }

    public boolean isBlackListed(String token) {
        Long expiry = blackListedTokens.get(token);
        if (expiry == null)
            return false;

        // AutoCleanup
        if (expiry < System.currentTimeMillis()) {
            blackListedTokens.remove(token);
            return false;
        }
        return true;

    }
}
