package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreakerRateLimiter implements RateLimiter {

    private final RateLimiter delegate;
    private final CircuitBreakerConfig config;
    private final ClockProvider clockProvider;

    private final AtomicReference<CircuitBreakerState> state =
            new AtomicReference<>(CircuitBreakerState.CLOSED);

    private final AtomicInteger consecutiveFailures =
            new AtomicInteger();

    private volatile long openTimestamp;

    public CircuitBreakerRateLimiter(
            RateLimiter delegate,
            CircuitBreakerConfig config,
            ClockProvider clockProvider) {

        this.delegate = Objects.requireNonNull(
                delegate,
                "Delegate rate limiter must not be null");

        this.config = Objects.requireNonNull(
                config,
                "Circuit breaker config must not be null");

        this.clockProvider = Objects.requireNonNull(
                clockProvider,
                "Clock provider must not be null");
    }

    CircuitBreakerState getState() {
        return state.get();
    }
    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule) {

        CircuitBreakerState currentState = state.get();
        if (currentState == CircuitBreakerState.OPEN) {
            if (shouldTransitionToHalfOpen()) {
                state.set(CircuitBreakerState.HALF_OPEN);
            } else {
                throw new CircuitBreakerOpenException();
            }
        }

        try {
            RateLimitResult result = delegate.tryAcquire(key, rule);
            onSuccess();
            return result;
        } catch (Exception exception) {
            onFailure();
            throw exception;
        }

    }

    private boolean shouldTransitionToHalfOpen() {

        long elapsedMillis = clockProvider.currentTimeMillis() - openTimestamp;
        return elapsedMillis >= config.waitDurationInOpenState()
                                        .toMillis();

    }

    private void onSuccess() {

        consecutiveFailures.set(0);
        state.set(CircuitBreakerState.CLOSED);
    }

    private void onFailure() {

        /*
         * In HALF_OPEN even a single failure should
         * immediately reopen the circuit.
         */
        if (state.get() == CircuitBreakerState.HALF_OPEN) {
            consecutiveFailures.set(0);
            reopenCircuit();
            return;
        }

        int failures = consecutiveFailures.incrementAndGet();

        if (failures >= config.failureThreshold()) {
            reopenCircuit();
        }

    }

    private void reopenCircuit() {

        state.set(CircuitBreakerState.OPEN);
        openTimestamp = clockProvider.currentTimeMillis();
    }
}