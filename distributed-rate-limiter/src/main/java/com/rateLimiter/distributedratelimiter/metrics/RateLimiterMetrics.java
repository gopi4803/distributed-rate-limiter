package com.rateLimiter.distributedratelimiter.metrics;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterMetrics {

    private static final String ALGORITHM_TAG = "algorithm";

    private final Map<Algorithm, Counter> allowedCounters =
            new EnumMap<>(Algorithm.class);

    private final Map<Algorithm, Counter> blockedCounters =
            new EnumMap<>(Algorithm.class);

    private final Map<Algorithm, Counter> redisFailureCounters =
            new EnumMap<>(Algorithm.class);

    private final Map<Algorithm, Counter> circuitBreakerOpenCounters =
            new EnumMap<>(Algorithm.class);

    private final Map<Algorithm, Timer> requestDurationTimers =
            new EnumMap<>(Algorithm.class);

    public RateLimiterMetrics(MeterRegistry meterRegistry) {

        Objects.requireNonNull(
                meterRegistry,
                "MeterRegistry must not be null");

        for (Algorithm algorithm : Algorithm.values()) {

            allowedCounters.put(
                    algorithm,
                    Counter.builder(
                                    "ratelimiter.requests.allowed")
                            .description(
                                    "Total allowed requests")
                            .tag(ALGORITHM_TAG, algorithm.name())
                            .register(meterRegistry));

            blockedCounters.put(
                    algorithm,
                    Counter.builder(
                                    "ratelimiter.requests.blocked")
                            .description(
                                    "Total blocked requests")
                            .tag(ALGORITHM_TAG, algorithm.name())
                            .register(meterRegistry));

            redisFailureCounters.put(
                    algorithm,
                    Counter.builder(
                                    "ratelimiter.redis.failures")
                            .description(
                                    "Total Redis failures")
                            .tag(ALGORITHM_TAG, algorithm.name())
                            .register(meterRegistry));

            circuitBreakerOpenCounters.put(
                    algorithm,
                    Counter.builder(
                                    "ratelimiter.circuitbreaker.open.transitions")
                            .description(
                                    "Total circuit breaker OPEN transitions")
                            .tag(ALGORITHM_TAG, algorithm.name())
                            .register(meterRegistry));

            requestDurationTimers.put(
                    algorithm,
                    Timer.builder(
                                    "ratelimiter.request.duration")
                            .description(
                                    "Rate limiter request duration")
                            .tag(ALGORITHM_TAG, algorithm.name())
                            .register(meterRegistry));
        }
    }

    public void recordAllowed(Algorithm algorithm) {
        allowedCounters.get(algorithm).increment();
    }

    public void recordBlocked(Algorithm algorithm) {
        blockedCounters.get(algorithm).increment();
    }

    public void recordRedisFailure(Algorithm algorithm) {
        redisFailureCounters.get(algorithm).increment();
    }

    public void recordCircuitBreakerOpened(Algorithm algorithm) {
        circuitBreakerOpenCounters.get(algorithm).increment();
    }

    public void recordRequestDuration(
            Algorithm algorithm,
            long durationNanos) {

        requestDurationTimers.get(algorithm)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}