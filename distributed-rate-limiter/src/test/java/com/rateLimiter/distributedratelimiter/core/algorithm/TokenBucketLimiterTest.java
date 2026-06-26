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

public class TokenBucketLimiterTest {

    @Test
    void shouldAllowExactlyLimitRequests(){
        MutableClockProvider clock=new MutableClockProvider(0);
        TokenBucketLimiter limiter=new TokenBucketLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",5, Duration.ofSeconds(10), Algorithm.TOKEN_BUCKET);
        for(int i=0;i<5;i++){
            RateLimitResult result=limiter.tryAcquire("user-1",rule);
            assertTrue(result.allowed());
        }
        assertFalse(limiter.tryAcquire("user-1",rule).allowed());
    }

    @Test
    void shouldRefillTokensAfterTimePasses(){
        MutableClockProvider clock=new MutableClockProvider(0);
        TokenBucketLimiter limiter=new TokenBucketLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofSeconds(10),Algorithm.TOKEN_BUCKET);
        for(int i=0;i<10;i++){
            limiter.tryAcquire("user-1",rule);
        }
        assertFalse(limiter.tryAcquire("user-1",rule).allowed());
        clock.advance(Duration.ofSeconds(2));
        assertTrue(limiter.tryAcquire("user-1",rule).allowed());
    }

    @Test
    void shouldKeepBucketsIndependentAcrossKeys(){
        MutableClockProvider clock=new MutableClockProvider(0);
        TokenBucketLimiter limiter=new TokenBucketLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",1,Duration.ofMinutes(1),Algorithm.TOKEN_BUCKET);
        assertTrue(limiter.tryAcquire("userA",rule).allowed());
        assertTrue(limiter.tryAcquire("userB",rule).allowed());
        assertFalse(limiter.tryAcquire("userA",rule).allowed());
    }

    @Test
    void shouldReturnRetryAfterWhenRequestIsBlocked(){
        MutableClockProvider clock=new MutableClockProvider(0);
        TokenBucketLimiter limiter=new TokenBucketLimiter(clock);
        RateLimitRule rule=new RateLimitRule("test",1,Duration.ofSeconds(10),Algorithm.TOKEN_BUCKET);
        limiter.tryAcquire("user-1",rule);
        RateLimitResult result=limiter.tryAcquire("user-1",rule);
        assertFalse(result.allowed());
        assertTrue(result.retryAfterMs()>0);
    }

    @Test
    void shouldNeverExceedLimitUnderConcurrency()
            throws InterruptedException {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        TokenBucketLimiter limiter =
                new TokenBucketLimiter(
                        clock);

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        10,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET);

        int threadCount = 100;
        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch =
                new CountDownLatch(threadCount);

        AtomicInteger allowedRequests =
                new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executor.submit(() -> {

                try {

                    if (limiter.tryAcquire(
                            "shared-key",
                            rule).allowed()) {

                        allowedRequests.incrementAndGet();
                    }

                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(
                latch.await(5, TimeUnit.SECONDS));

        executor.shutdown();

        assertEquals(
                10,
                allowedRequests.get());
    }

    @Test
    void shouldRejectBlankKey() {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        TokenBucketLimiter limiter =
                new TokenBucketLimiter(
                        clock);

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        1,
                        Duration.ofSeconds(1),
                        Algorithm.TOKEN_BUCKET);

        assertThrows(
                IllegalArgumentException.class,
                () -> limiter.tryAcquire(" ", rule));
    }

    @Test
    void shouldRejectInvalidLimit() {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        TokenBucketLimiter limiter =
                new TokenBucketLimiter(
                        clock);

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        0,
                        Duration.ofSeconds(1),
                        Algorithm.TOKEN_BUCKET);

        assertThrows(
                IllegalArgumentException.class,
                () -> limiter.tryAcquire("user", rule));
    }

    @Test
    void shouldRejectInvalidWindow() {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        TokenBucketLimiter limiter =
                new TokenBucketLimiter(
                        clock);

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        10,
                        Duration.ZERO,
                        Algorithm.TOKEN_BUCKET);

        assertThrows(
                IllegalArgumentException.class,
                () -> limiter.tryAcquire("user", rule));
    }

    @Test
    void shouldCapTokensAtBucketCapacity() {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        TokenBucketLimiter limiter =
                new TokenBucketLimiter(clock);

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        10,
                        Duration.ofSeconds(10),
                        Algorithm.TOKEN_BUCKET);

        clock.advance(Duration.ofHours(1));

        RateLimitResult result =
                limiter.tryAcquire("user-1", rule);

        assertTrue(result.allowed());
        assertEquals(9, result.remaining());
    }
}
