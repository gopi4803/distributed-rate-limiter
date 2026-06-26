package com.rateLimiter.distributedratelimiter.core.utils;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;

import java.util.Objects;

public final class ValidationUtils {

    private ValidationUtils(){}

    public static void validateInputs(String key, RateLimitRule rule){
        Objects.requireNonNull(key,"Key must not be null");
        Objects.requireNonNull(rule,"Rule must not be null");

        if(key.isBlank()) throw new IllegalArgumentException("Key must not be blank");
        if(rule.limit()<=0) throw new IllegalArgumentException("Limit must be greater than 0");
        if(rule.window().isZero() || rule.window().isNegative()) throw new IllegalArgumentException("Window must be greater than 0");
    }
}
