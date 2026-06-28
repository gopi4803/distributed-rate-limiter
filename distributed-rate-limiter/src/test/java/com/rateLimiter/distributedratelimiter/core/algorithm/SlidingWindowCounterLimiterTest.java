package com.rateLimiter.distributedratelimiter.core.algorithm;

import com.rateLimiter.distributedratelimiter.core.clock.MutableClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowCounterLimiterTest {
    @Test
    void shouldAllowRequestsWithinLimit(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",5, Duration.ofSeconds(10), Algorithm.SLIDING_WINDOW_COUNTER);
        for(int i=0;i<5;i++){
            assertTrue(limiter.tryAcquire("user-1",rule).allowed());
        }
    }

    @Test
    void shouldBlockRequestsBeyondLimit(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",5,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        for(int i=0;i<5;i++){
            limiter.tryAcquire("user-1",rule);
        }
        assertFalse(limiter.tryAcquire("user-1",rule).allowed());
    }

    @Test
    void shouldGraduallyAgeOutPreviousWindow(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        for(int i=0;i<10;i++){
            limiter.tryAcquire("user-1",rule);
        }
        clock.advance(Duration.ofSeconds(11));
        assertTrue(limiter.tryAcquire("user",rule).allowed());
    }

    @Test
    void shouldResetCountWhenMultipleWindowsPass(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",5,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        for(int i=0;i<10;i++){
            limiter.tryAcquire("user-1",rule);
        }
        clock.advance(Duration.ofSeconds(50));
        RateLimitResult result=limiter.tryAcquire("user-1",rule);
        assertTrue(result.allowed());
    }

    @Test
    void shouldKeepWindowsIndependentAcrossKeys(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",1,Duration.ofMinutes(1),Algorithm.SLIDING_WINDOW_COUNTER);
        assertTrue(limiter.tryAcquire("userA",rule).allowed());
        assertTrue(limiter.tryAcquire("userB",rule).allowed());
        assertTrue(limiter.tryAcquire("userC",rule).allowed());
        assertFalse(limiter.tryAcquire("userB",rule).allowed());
    }

    @Test
    void shouldReturnRetryAfterBlocked(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",1,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        limiter.tryAcquire("userA",rule);
        RateLimitResult result=limiter.tryAcquire("userA",rule);
        assertFalse(result.allowed());
        assertTrue(result.retryAfterMs()>0);
    }

    @Test
    void shouldNeverExceedLimitUnderConcurrency() throws InterruptedException{
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofMinutes(1),Algorithm.SLIDING_WINDOW_COUNTER);
        int threadCount=100;
        ExecutorService executor= Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch=new CountDownLatch(threadCount);
        AtomicInteger allowedRequests=new AtomicInteger();
        for(int i=0;i<threadCount;i++){
            executor.submit(()->{
                try {
                    if(limiter.tryAcquire("shared-key",rule).allowed()){
                        allowedRequests.incrementAndGet();
                    }
                }finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        assertEquals(10,allowedRequests.get());
    }

    @Test
    void shouldRejectBlankKey(){
        MutableClockProvider clock=new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter=new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",5,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        assertThrows(IllegalArgumentException.class,()->limiter.tryAcquire("",rule));
    }

    @Test
    void shouldAllowRequestWhenPreviousWindowContributionHasExpired() {
        MutableClockProvider clock = new MutableClockProvider(0);
        SlidingWindowCounterLimiter limiter = new SlidingWindowCounterLimiter(clock);
        RateLimitRule rule = new RateLimitRule("test",5,Duration.ofSeconds(10),Algorithm.SLIDING_WINDOW_COUNTER);
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire("user", rule);
        }
        clock.advance(Duration.ofSeconds(25));
        assertTrue(limiter.tryAcquire("user", rule).allowed());
    }

}
