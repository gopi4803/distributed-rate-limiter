package com.rateLimiter.distributedratelimiter.core.utils;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.InvalidRateLimitRuleException;

import java.util.Objects;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validateInputs(String key, RateLimitRule rule) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(rule, "Rule must not be null");

        if (key.isBlank()) throw new InvalidRateLimitRuleException("Key must not be blank");
    }
}
