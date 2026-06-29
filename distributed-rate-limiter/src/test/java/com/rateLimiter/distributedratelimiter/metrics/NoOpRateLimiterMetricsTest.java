package com.rateLimiter.distributedratelimiter.metrics;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NoOpRateLimiterMetricsTest {

    @Test
    void shouldNotThrowWhenRecordingAllowedRequests() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();

        assertDoesNotThrow(() ->
                metrics.recordAllowed(Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldNotThrowWhenRecordingBlockedRequests() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();

        assertDoesNotThrow(() ->
                metrics.recordBlocked(Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldNotThrowWhenRecordingRedisFailures() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();

        assertDoesNotThrow(() ->
                metrics.recordRedisFailure(Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldNotThrowWhenRecordingCircuitBreakerOpenTransitions() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();

        assertDoesNotThrow(() ->
                metrics.recordCircuitBreakerOpened(
                        Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldNotThrowWhenRecordingRequestDuration() {

        RateLimiterMetrics metrics =
                new NoOpRateLimiterMetrics();

        assertDoesNotThrow(() ->
                metrics.recordRequestDuration(
                        Algorithm.TOKEN_BUCKET,
                        1_000_000));
    }
}