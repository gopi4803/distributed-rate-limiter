package com.rateLimiter.distributedratelimiter.core;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;

public interface RateLimiter {
    RateLimitResult tryAcquire(String key, RateLimitRule rule);
}
