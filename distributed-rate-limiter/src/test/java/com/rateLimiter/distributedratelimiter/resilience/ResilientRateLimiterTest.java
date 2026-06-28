package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;
import com.rateLimiter.distributedratelimiter.exceptions.RedisExecutionException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResilientRateLimiterTest {

    private static final RateLimitRule RULE =
            new RateLimitRule(
                    "test",
                    10,
                    Duration.ofMinutes(1),
                    Algorithm.TOKEN_BUCKET);

    private static class RedisFailingLimiter
            implements RateLimiter {

        @Override
        public RateLimitResult tryAcquire(
                String key,
                RateLimitRule rule) {

            throw new RedisExecutionException(
                    "Redis unavailable",
                    new RuntimeException("Redis down"));
        }
    }

    private static class CircuitOpenLimiter
            implements RateLimiter {

        @Override
        public RateLimitResult tryAcquire(
                String key,
                RateLimitRule rule) {

            throw new CircuitBreakerOpenException();
        }
    }

    private static class InvalidInputLimiter
            implements RateLimiter {

        @Override
        public RateLimitResult tryAcquire(
                String key,
                RateLimitRule rule) {

            throw new IllegalArgumentException(
                    "Invalid rule");
        }
    }

    @Test
    void shouldDelegateWhenNoFailure() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                9,
                                0));

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        delegate,
                        FailureStrategy.FAIL_OPEN);

        RateLimitResult result =
                resilient.tryAcquire(
                        "user",
                        RULE);

        assertTrue(result.allowed());
        assertEquals(9, result.remaining());
        assertEquals(0, result.retryAfterMs());
    }

    @Test
    void shouldAllowRequestWhenFailOpenAndRedisFails() {

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        new RedisFailingLimiter(),
                        FailureStrategy.FAIL_OPEN);

        RateLimitResult result =
                resilient.tryAcquire(
                        "user",
                        RULE);

        assertTrue(result.allowed());
        assertEquals(-1, result.remaining());
        assertEquals(0, result.retryAfterMs());
    }

    @Test
    void shouldRejectRequestWhenFailClosedAndRedisFails() {

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        new RedisFailingLimiter(),
                        FailureStrategy.FAIL_CLOSED);

        RateLimitResult result =
                resilient.tryAcquire(
                        "user",
                        RULE);

        assertFalse(result.allowed());
        assertEquals(0, result.remaining());
        assertEquals(
                Long.MAX_VALUE,
                result.retryAfterMs());
    }

    @Test
    void shouldAllowRequestWhenCircuitIsOpenAndFailOpen() {

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        new CircuitOpenLimiter(),
                        FailureStrategy.FAIL_OPEN);

        RateLimitResult result =
                resilient.tryAcquire(
                        "user",
                        RULE);

        assertTrue(result.allowed());
        assertEquals(-1, result.remaining());
    }

    @Test
    void shouldRejectRequestWhenCircuitIsOpenAndFailClosed() {

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        new CircuitOpenLimiter(),
                        FailureStrategy.FAIL_CLOSED);

        RateLimitResult result =
                resilient.tryAcquire(
                        "user",
                        RULE);

        assertFalse(result.allowed());
    }

    @Test
    void shouldThrowWhenDelegateIsNull() {

        assertThrows(
                NullPointerException.class,
                () -> new ResilientRateLimiter(
                        null,
                        FailureStrategy.FAIL_OPEN));
    }

    @Test
    void shouldThrowWhenFailureStrategyIsNull() {

        RateLimiter delegate = mock(RateLimiter.class);

        assertThrows(
                NullPointerException.class,
                () -> new ResilientRateLimiter(
                        delegate,
                        null));
    }

    @Test
    void shouldNotFallbackForApplicationExceptions() {

        ResilientRateLimiter resilient =
                new ResilientRateLimiter(
                        new InvalidInputLimiter(),
                        FailureStrategy.FAIL_OPEN);

        assertThrows(
                IllegalArgumentException.class,
                () -> resilient.tryAcquire(
                        "user",
                        RULE));
    }
}