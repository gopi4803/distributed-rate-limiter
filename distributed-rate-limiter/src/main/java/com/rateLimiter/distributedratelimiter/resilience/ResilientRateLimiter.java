package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;
import com.rateLimiter.distributedratelimiter.exceptions.RedisExecutionException;
import lombok.Getter;

import java.util.Objects;

public class ResilientRateLimiter implements RateLimiter {

    @Getter
    private final RateLimiter delegate;
    private final FailureStrategy failureStrategy;

    public ResilientRateLimiter(RateLimiter delegate,FailureStrategy failureStrategy){
        this.delegate= Objects.requireNonNull(delegate,"Delegate must be non null");
        this.failureStrategy=Objects.requireNonNull(failureStrategy,"Failure Strategy must be non null");
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule){
        try {
            return delegate.tryAcquire(key,rule);
        }catch (RedisExecutionException |
                CircuitBreakerOpenException exception) {
            return handleFailure();
        }
    }

    private RateLimitResult handleFailure(){
        if(failureStrategy==FailureStrategy.FAIL_OPEN){
            return new RateLimitResult(true,-1,0);
        }
        return new RateLimitResult(false,0,Long.MAX_VALUE);
    }


}
