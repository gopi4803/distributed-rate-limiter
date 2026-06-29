package com.rateLimiter.distributedratelimiter.policy;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.key.KeyExtractor;

import java.util.Objects;

public record RateLimitPolicy(KeyExtractor extractor, RateLimitRule rule) {
    public RateLimitPolicy{
        Objects.requireNonNull(extractor,"Extractor must be non null");
        Objects.requireNonNull(rule,"Rule must be non null");
    }
}
