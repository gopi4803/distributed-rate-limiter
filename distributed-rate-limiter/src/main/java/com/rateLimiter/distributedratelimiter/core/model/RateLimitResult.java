package com.rateLimiter.distributedratelimiter.core.model;

public record RateLimitResult(boolean allowed,long remaining,long retryAfterMs) {
}
