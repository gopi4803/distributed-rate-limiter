package com.rateLimiter.distributedratelimiter.core.clock;

public interface ClockProvider {
    long currentTimeMillis();
    long nanoTime();
}
