package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;

public class ResilientRateLimiter implements RateLimiter {

    private final RateLimiter delegate;
    private final FailureStrategy failureStrategy;

    public ResilientRateLimiter(RateLimiter delegate,FailureStrategy failureStrategy){
        this.delegate=delegate;
        this.failureStrategy=failureStrategy;
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule){
        try {
            return delegate.tryAcquire(key,rule);
        }catch (Exception exception){
            return handleFailure(exception);
        }
    }

    private RateLimitResult handleFailure(Exception exception){
        if(failureStrategy==FailureStrategy.FAIL_OPEN){
            return new RateLimitResult(true,Long.MAX_VALUE,0);
        }
        return new RateLimitResult(false,0,Long.MAX_VALUE);
    }


}
