package com.rateLimiter.distributedratelimiter.core.model;

import com.rateLimiter.distributedratelimiter.exceptions.InvalidRateLimitRuleException;

import java.time.Duration;
import java.util.Objects;

public record RateLimitRule(String name, int limit, Duration window,Algorithm algorithm) {
    public RateLimitRule{
        Objects.requireNonNull(name,"Rate limit rule name must be non null");
        Objects.requireNonNull(window,"Rate limit rule window must be non null");
        Objects.requireNonNull(algorithm,"Rate limit rule algorithm must be non null");

        if(name.isBlank()) throw new InvalidRateLimitRuleException("Rate limit rule name must not be blank");
        if(limit<=0) throw new InvalidRateLimitRuleException("Rate limit rule limit must be greater than zero");
        if(window.isZero() || window.isNegative()) throw new InvalidRateLimitRuleException("Rate limit rule window must be greater than zero");

    }
}
