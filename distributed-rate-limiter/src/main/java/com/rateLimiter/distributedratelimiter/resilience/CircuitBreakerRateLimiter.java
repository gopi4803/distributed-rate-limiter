package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerOpenException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class CircuitBreakerRateLimiter implements RateLimiter {

    private final RateLimiter delegate;
    private final CircuitBreakerConfig config;
    private final ClockProvider clockProvider;

    private final AtomicReference<CircuitBreakerState> state =
            new AtomicReference<>(CircuitBreakerState.CLOSED);

    private final AtomicInteger consecutiveFailures =
            new AtomicInteger();

    private final AtomicBoolean probeInProgress =
            new AtomicBoolean(false);

    private final ReentrantLock stateTransitionLock=new ReentrantLock();

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

    RateLimiter getDelegate() {
        return delegate;
    }

    CircuitBreakerState getState() {
        return state.get();
    }

    private void beforeRequest() {
        while (true) {
            CircuitBreakerState currentState = state.get();

            switch (currentState) {
                case CLOSED:
                    return;

                case OPEN:
                    if (!shouldTransitionToHalfOpen()) {
                        throw new CircuitBreakerOpenException();
                    }
                    transitionToHalfOpen();
                    // state changed, re-evaluate
                    continue;

                case HALF_OPEN:
                    if (probeInProgress.compareAndSet(false, true)) {
                        return;
                    }
                    throw new CircuitBreakerOpenException();
            }
        }
    }

    private void transitionToHalfOpen() {
        if (!stateTransitionLock.tryLock()) {
            throw new CircuitBreakerOpenException();
        }
        try {
            if (state.get() != CircuitBreakerState.OPEN) {
                return;
            }
            if (!shouldTransitionToHalfOpen()) {
                throw new CircuitBreakerOpenException();
            }
            state.set(CircuitBreakerState.HALF_OPEN);
            probeInProgress.set(false);
        } finally {
            stateTransitionLock.unlock();
        }
    }

    @Override
    public RateLimitResult tryAcquire(
            String key,
            RateLimitRule rule) {

        beforeRequest();
        try {
            RateLimitResult result = delegate.tryAcquire(key, rule);
            onSuccess();
            return result;
        } catch (Exception ex) {
            onFailure();
            throw ex;
        }
    }

    private boolean shouldTransitionToHalfOpen() {

        long elapsedMillis = clockProvider.currentTimeMillis() - openTimestamp;
        return elapsedMillis >= config.waitDurationInOpenState()
                                        .toMillis();

    }

    private void onSuccess() {

        consecutiveFailures.set(0);
        if (state.get() == CircuitBreakerState.HALF_OPEN) {
            state.set(CircuitBreakerState.CLOSED);
            probeInProgress.set(false);
        }
    }

    private void onFailure() {

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
        probeInProgress.set(false);
    }
}