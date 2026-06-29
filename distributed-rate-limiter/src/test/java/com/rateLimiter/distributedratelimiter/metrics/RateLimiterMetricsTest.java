package com.rateLimiter.distributedratelimiter.metrics;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private RateLimiterMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new RateLimiterMetrics(meterRegistry);
    }

    @Test
    void shouldRecordAllowedRequest() {

        metrics.recordAllowed(Algorithm.TOKEN_BUCKET);

        double count = meterRegistry.get(
                        "ratelimiter.requests.allowed")
                .tag("algorithm", "TOKEN_BUCKET")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordBlockedRequest() {

        metrics.recordBlocked(Algorithm.TOKEN_BUCKET);

        double count = meterRegistry.get(
                        "ratelimiter.requests.blocked")
                .tag("algorithm", "TOKEN_BUCKET")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordRedisFailure() {

        metrics.recordRedisFailure(Algorithm.TOKEN_BUCKET);

        double count = meterRegistry.get(
                        "ratelimiter.redis.failures")
                .tag("algorithm", "TOKEN_BUCKET")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordCircuitBreakerOpenTransition() {

        metrics.recordCircuitBreakerOpened(
                Algorithm.TOKEN_BUCKET);

        double count = meterRegistry.get(
                        "ratelimiter.circuitbreaker.open.transitions")
                .tag("algorithm", "TOKEN_BUCKET")
                .counter()
                .count();

        assertEquals(1.0, count);
    }

    @Test
    void shouldRecordRequestDuration() {

        metrics.recordRequestDuration(
                Algorithm.TOKEN_BUCKET,
                1_000_000);

        double count = meterRegistry.get(
                        "ratelimiter.request.duration")
                .tag("algorithm", "TOKEN_BUCKET")
                .timer()
                .count();

        assertEquals(1, (long) count);
    }
}