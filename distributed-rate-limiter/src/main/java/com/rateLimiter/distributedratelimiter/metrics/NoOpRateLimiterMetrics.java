package com.rateLimiter.distributedratelimiter.metrics;

public class NoOpRateLimiterMetrics implements RateLimiterMetrics {

    @Override
    public void recordAllowedRequests(){

    }

    @Override
    public void recordBlockedRequests(){

    }

    @Override
    public void recordRedisFailures(){

    }

    @Override
    public void recordCircuitBreakerOpen(){

    }

}
