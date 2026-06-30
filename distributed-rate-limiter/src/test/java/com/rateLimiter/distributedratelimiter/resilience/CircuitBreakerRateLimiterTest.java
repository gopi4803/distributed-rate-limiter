package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.clock.MutableClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;
import com.rateLimiter.distributedratelimiter.exceptions.RedisExecutionException;
import com.rateLimiter.distributedratelimiter.metrics.NoOpRateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.SLIDING_WINDOW_COUNTER);

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
                        clock,new NoOpRateLimiterMetrics(),Algorithm.SLIDING_WINDOW_COUNTER);

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

    @Test
    void shouldAllowOnlySingleProbeRequestInHalfOpenState()
            throws Exception {

        RateLimiter delegate = mock(RateLimiter.class);

        // Open the circuit first
        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock,new NoOpRateLimiterMetrics(),Algorithm.SLIDING_WINDOW_COUNTER);

        // Reach failure threshold
        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire(
                            "user",
                            rule));
        }

        assertEquals(
                CircuitBreakerState.OPEN,
                circuitBreaker.getState());

        // Move breaker into HALF_OPEN eligibility
        clock.advance(Duration.ofSeconds(31));

        CountDownLatch probeEntered =
                new CountDownLatch(1);

        CountDownLatch releaseProbe =
                new CountDownLatch(1);

        reset(delegate);

        when(delegate.tryAcquire(any(), any()))
                .thenAnswer(invocation -> {

                    probeEntered.countDown();

                    // Keep probe blocked so all other
                    // threads observe HALF_OPEN
                    releaseProbe.await(
                            5,
                            TimeUnit.SECONDS);

                    return new RateLimitResult(
                            true,
                            9,
                            0);
                });

        int threadCount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CyclicBarrier startBarrier =
                new CyclicBarrier(threadCount);

        CountDownLatch allDone =
                new CountDownLatch(threadCount);

        AtomicInteger successfulRequests =
                new AtomicInteger();

        AtomicInteger rejectedRequests =
                new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executor.submit(() -> {

                try {

                    // Start all threads simultaneously
                    startBarrier.await();

                    circuitBreaker.tryAcquire(
                            "user",
                            rule);

                    successfulRequests.incrementAndGet();

                } catch (CircuitBreakerOpenException ex) {

                    rejectedRequests.incrementAndGet();

                } catch (Exception ex) {

                    fail("Unexpected exception: " + ex);

                } finally {

                    allDone.countDown();
                }
            });
        }

        // Wait until exactly one thread enters delegate
        assertTrue(
                probeEntered.await(
                        5,
                        TimeUnit.SECONDS));

        // While probe is blocked, only one invocation
        // should have reached delegate
        verify(delegate, timeout(1000).times(1))
                .tryAcquire(any(), any());

        // Release the probe
        releaseProbe.countDown();

        assertTrue(
                allDone.await(
                        5,
                        TimeUnit.SECONDS));

        executor.shutdown();

        assertTrue(successfulRequests.get() >= 1);

        assertTrue(rejectedRequests.get() >= 1);

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());
    }

    @Test
    void shouldThrowWhenDelegateIsNull() {

        MutableClockProvider clock =
                new MutableClockProvider(0);

        assertThrows(
                NullPointerException.class,
                () -> new CircuitBreakerRateLimiter(
                        null,
                        config,
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldThrowWhenConfigIsNull() {

        RateLimiter delegate = mock(RateLimiter.class);

        MutableClockProvider clock =
                new MutableClockProvider(0);

        assertThrows(
                NullPointerException.class,
                () -> new CircuitBreakerRateLimiter(
                        delegate,
                        null,
                        clock,new NoOpRateLimiterMetrics(),Algorithm.SLIDING_WINDOW_COUNTER));
    }

    @Test
    void shouldThrowWhenClockProviderIsNull() {

        RateLimiter delegate = mock(RateLimiter.class);

        assertThrows(
                NullPointerException.class,
                () -> new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        null,new NoOpRateLimiterMetrics(),Algorithm.SLIDING_WINDOW_COUNTER));
    }

    @Test
    void shouldResetFailureCountAfterSuccessfulRequest() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                9,
                                0));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

        assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        circuitBreaker.tryAcquire("user", rule);

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());

        // One more failure should NOT open the circuit

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        assertThrows(
                RuntimeException.class,
                () -> circuitBreaker.tryAcquire("user", rule));

        assertEquals(
                CircuitBreakerState.CLOSED,
                circuitBreaker.getState());
    }

    @Test
    void shouldContinueRejectingRequestsWhileCircuitIsOpen() {

        RateLimiter delegate = mock(RateLimiter.class);

        when(delegate.tryAcquire(any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        MutableClockProvider clock =
                new MutableClockProvider(0);

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clock,new NoOpRateLimiterMetrics(),Algorithm.TOKEN_BUCKET);

        for (int i = 0; i < 3; i++) {

            assertThrows(
                    RuntimeException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        for (int i = 0; i < 10; i++) {

            assertThrows(
                    CircuitBreakerOpenException.class,
                    () -> circuitBreaker.tryAcquire("user", rule));
        }

        verify(delegate, times(3))
                .tryAcquire(any(), any());
    }
    @Test
    void shouldRecordMetricWhenCircuitTransitionsToOpen() {

        SimpleMeterRegistry meterRegistry =
                new SimpleMeterRegistry();

        RateLimiterMetrics metrics =
                new RateLimiterMetrics(meterRegistry);

        RateLimiter delegate =
                mock(RateLimiter.class);

        ClockProvider clockProvider =
                mock(ClockProvider.class);

        CircuitBreakerConfig config =
                new CircuitBreakerConfig(
                        1,
                        Duration.ofSeconds(30));

        CircuitBreakerRateLimiter circuitBreaker =
                new CircuitBreakerRateLimiter(
                        delegate,
                        config,
                        clockProvider,
                        metrics,
                        Algorithm.TOKEN_BUCKET);

        RateLimitRule rule =
                new RateLimitRule(
                        "test-rule",
                        5,
                        Duration.ofSeconds(10),
                        Algorithm.TOKEN_BUCKET);

        when(delegate.tryAcquire(
                anyString(),
                any(RateLimitRule.class)))
                .thenThrow(
                        new RedisExecutionException(
                                "Redis unavailable",
                                null));

        assertThrows(
                RedisExecutionException.class,
                () -> circuitBreaker.tryAcquire(
                        "user-1",
                        rule));

        double count =
                meterRegistry.get(
                                "ratelimiter.circuitbreaker.open.transitions")
                        .tag(
                                "algorithm",
                                "TOKEN_BUCKET")
                        .counter()
                        .count();

        assertEquals(1.0, count);
    }
}