package com.rateLimiter.distributedratelimiter.key;

import jakarta.servlet.http.HttpServletRequest;

public class UserIdKeyExtractor implements KeyExtractor{
    @Override
    public String extract(HttpServletRequest request){
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return prefix() + ":" + userId;
    }

    @Override
    public String prefix() {
        return "user";
    }
}
