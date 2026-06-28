package com.rateLimiter.distributedratelimiter.resilience;

public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
