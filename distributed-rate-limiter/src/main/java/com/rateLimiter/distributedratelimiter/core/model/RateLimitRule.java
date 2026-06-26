package com.rateLimiter.distributedratelimiter.core.model;

import java.time.Duration;

public record RateLimitRule(String name, int limit, Duration window,Algorithm algorithm) {

}
