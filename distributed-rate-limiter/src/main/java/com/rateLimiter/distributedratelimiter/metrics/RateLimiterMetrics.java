package com.rateLimiter.distributedratelimiter.metrics;

public interface RateLimiterMetrics {
    void recordAllowedRequests();
    void recordBlockedRequests();
    void recordRedisFailures();
    void recordCircuitBreakerOpen();
}
