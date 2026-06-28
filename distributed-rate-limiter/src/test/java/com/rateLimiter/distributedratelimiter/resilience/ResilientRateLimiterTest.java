package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class ResilientRateLimiterTest {

    private static class FailingLimiter implements RateLimiter{
        @Override
        public RateLimitResult tryAcquire(String key, RateLimitRule rule){
            throw new RuntimeException("Redis down");
        }
    }

    @Test
    void shouldDelegateWhenNoFailure(){
        RateLimiter limiter=mock(RateLimiter.class);
        RateLimitRule rule=new RateLimitRule("test",10, Duration.ofMinutes(1), Algorithm.TOKEN_BUCKET);
        when(limiter.tryAcquire(any(),any()))
                .thenReturn(new RateLimitResult(true,9,0));
        ResilientRateLimiter resilient=new ResilientRateLimiter(limiter,FailureStrategy.FAIL_OPEN);
        RateLimitResult result=resilient.tryAcquire("user",rule);
        assertTrue(result.allowed());
        assertEquals(9,result.remaining());
    }

    @Test
    void shouldAllowRequestWhenOpenAndDelegateFails(){
        ResilientRateLimiter resilient=new ResilientRateLimiter(new FailingLimiter(),FailureStrategy.FAIL_OPEN);
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofMinutes(1),Algorithm.TOKEN_BUCKET);
        RateLimitResult result=resilient.tryAcquire("user",rule);
        assertTrue(result.allowed());
    }

    @Test
    void shouldRejectRequestWhenFailClosedAndDelegateFails(){
        ResilientRateLimiter resilient=new ResilientRateLimiter(new FailingLimiter(),FailureStrategy.FAIL_CLOSED);
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofMinutes(1),Algorithm.SLIDING_WINDOW_COUNTER);
        RateLimitResult result=resilient.tryAcquire("user",rule);
        assertFalse(result.allowed());
    }

}
