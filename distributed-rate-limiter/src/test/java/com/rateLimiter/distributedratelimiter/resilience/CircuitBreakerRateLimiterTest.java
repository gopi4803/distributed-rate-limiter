package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.MutableClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CircuitBreakerRateLimiterTest {

    private final RateLimitRule rule =
            new RateLimitRule(
                    "test",
                    10,
                    Duration.ofMinutes(1),
                    Algorithm.TOKEN_BUCKET);

    private final CircuitBreakerConfig config =
            new CircuitBreakerConfig(
                    3,
                    Duration.ofSeconds(30));

    @Test
    void shouldDelegateWhenCircuitClosed() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenReturn(new RateLimitResult(true, 9, 0));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        RateLimitResult result =
                circuitBreaker.tryAcquire("user", rule);

        assertTrue(result.allowed());

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());

        verify(delegate, times(1))
                .tryAcquire("user", rule);
    }

    @Test
    void shouldOpenCircuitAfterFailureThresholdReached() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        assertEquals(
                CircuitBreakerState.OPEN,
                circuitBreaker.getState());
    }

    @Test
    void shouldRejectRequestsWhenCircuitIsOpen() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        assertThrows(
                CircuitBreakerOpenException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        verify(delegate, times(3))
                .tryAcquire(any(), any());
    }

    @Test
    void shouldTransitionToHalfOpenAfterWaitDuration() {

        RateLimiter delegate = mock(RateLimiter.class);

        // First 3 calls fail -> circuit opens
        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        assertEquals(
                CircuitBreakerState.OPEN,
                circuitBreaker.getState());

        // Advance time beyond wait duration
        clock.advance(Duration.ofSeconds(31));

        // Reset mock behavior for HALF_OPEN probe request
        reset(delegate);

        when(delegate.tryAcquire(any(), any()))
                .thenReturn(new RateLimitResult(true, 9, 0));

        RateLimitResult result =
                circuitBreaker.tryAcquire("user", rule);

        assertTrue(result.allowed());

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());
    }

    @Test
    void shouldCloseCircuitAfterSuccessfulHalfOpenRequest() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenReturn(new RateLimitResult(true, 9, 0));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        RateLimitResult result =
                circuitBreaker.tryAcquire("user", rule);

        assertTrue(result.allowed());

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());
    }

    @Test
    void shouldReopenCircuitWhenHalfOpenRequestFails() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        assertEquals(
                CircuitBreakerState.OPEN,
                circuitBreaker.getState());

        clock.advance(Duration.ofSeconds(31));

        assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        assertEquals(
                CircuitBreakerState.OPEN,
                circuitBreaker.getState());
    }

    @Test
    void shouldRemainClosedWhenFailuresAreBelowThreshold() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock);

        assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());
    }
}