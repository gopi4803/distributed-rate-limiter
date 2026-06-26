package com.rateLimiter.distributedratelimiter.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NoOpRateLimiterMetricsTest {
    @Test
    void shouldNotThrowWhenIncrementingAllowedRequests(){
        RateLimiterMetrics metrics=new NoOpRateLimiterMetrics();
        assertDoesNotThrow(metrics::recordAllowedRequests);
    }

    @Test
    void shouldNotThrowWhenIncrementingBlockedRequests() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();
        assertDoesNotThrow(metrics::recordBlockedRequests);
    }

    @Test
    void shouldNotThrowWhenIncrementingRedisFailures() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();
        assertDoesNotThrow(metrics::recordRedisFailures);
    }

    @Test
    void shouldNotThrowWhenIncrementingCircuitBreakerOpen() {
        RateLimiterMetrics metrics = new NoOpRateLimiterMetrics();
        assertDoesNotThrow(metrics::recordCircuitBreakerOpen);
    }
}
