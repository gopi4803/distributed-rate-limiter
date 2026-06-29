package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;
import com.rateLimiter.distributedratelimiter.exceptions.RedisExecutionException;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import lombok.Getter;

import java.util.Objects;

public class ResilientRateLimiter implements RateLimiter {

    @Getter
    private final RateLimiter delegate;
    private final FailureStrategy failureStrategy;
    private final RateLimiterMetrics metrics;
    private final Algorithm algorithm;

    public ResilientRateLimiter(RateLimiter delegate,FailureStrategy failureStrategy,RateLimiterMetrics metrics,Algorithm algorithm){
        this.delegate= Objects.requireNonNull(delegate,"Delegate must be non null");
        this.failureStrategy=Objects.requireNonNull(failureStrategy,"Failure Strategy must be non null");
        this.metrics=Objects.requireNonNull(metrics,"Metrics must be non null");
        this.algorithm=Objects.requireNonNull(algorithm,"Algorithm must be non null");
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule){
        long startTime=System.nanoTime();
        try {
            RateLimitResult result=delegate.tryAcquire(key,rule);
            if(result.allowed()) metrics.recordAllowed(algorithm);
            else metrics.recordBlocked(algorithm);
            return result;
        }catch (RedisExecutionException |
                CircuitBreakerOpenException exception) {
            metrics.recordRedisFailure(algorithm);
            return handleFailure();
        } finally {
            long durationNanos=System.nanoTime()-startTime;
            metrics.recordRequestDuration(algorithm,durationNanos);
        }
    }

    private RateLimitResult handleFailure(){
        if(failureStrategy==FailureStrategy.FAIL_OPEN){
            return new RateLimitResult(true,-1,0);
        }
        return new RateLimitResult(false,0,Long.MAX_VALUE);
    }


}
