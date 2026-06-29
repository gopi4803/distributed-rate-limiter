package com.rateLimiter.distributedratelimiter.metrics;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;

public class NoOpRateLimiterMetrics extends RateLimiterMetrics {

    public NoOpRateLimiterMetrics() {
        super(new io.micrometer.core.instrument.simple.SimpleMeterRegistry());
    }

    @Override
    public void recordAllowed(Algorithm algorithm) {
    }

    @Override
    public void recordBlocked(Algorithm algorithm) {
    }

    @Override
    public void recordRedisFailure(Algorithm algorithm) {
    }

    @Override
    public void recordCircuitBreakerOpened(Algorithm algorithm) {
    }

    @Override
    public void recordRequestDuration(
            Algorithm algorithm,
            long durationNanos) {
    }
}