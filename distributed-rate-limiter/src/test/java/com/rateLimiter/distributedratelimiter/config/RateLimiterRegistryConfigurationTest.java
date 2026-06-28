package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.resilience.CircuitBreakerRateLimiter;
import com.rateLimiter.distributedratelimiter.resilience.ResilientRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RateLimiterRegistryConfigurationTest {

    @Autowired
    private RateLimiterRegistry registry;

    @Test
    void shouldCreateRateLimiterRegistryBean() {

        assertNotNull(registry);
    }

    @Test
    void shouldRegisterTokenBucketLimiter() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.TOKEN_BUCKET);

        assertNotNull(limiter);
    }

    @Test
    void shouldRegisterSlidingWindowLimiter() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.SLIDING_WINDOW_COUNTER);

        assertNotNull(limiter);
    }

    @Test
    void shouldRegisterFixedWindowLimiter() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.FIXED_WINDOW);

        assertNotNull(limiter);
    }

    @Test
    void shouldRegisterAllAlgorithms() {

        for (Algorithm algorithm : Algorithm.values()) {

            assertNotNull(
                    registry.getLimiter(algorithm));
        }
    }

    @Test
    void shouldWrapTokenBucketLimiterWithResilienceLayer() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.TOKEN_BUCKET);

        assertInstanceOf(
                ResilientRateLimiter.class,
                limiter);
    }

    @Test
    void shouldWrapSlidingWindowLimiterWithResilienceLayer() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.SLIDING_WINDOW_COUNTER);

        assertInstanceOf(
                ResilientRateLimiter.class,
                limiter);
    }

    @Test
    void shouldWrapFixedWindowLimiterWithResilienceLayer() {

        RateLimiter limiter =
                registry.getLimiter(
                        Algorithm.FIXED_WINDOW);

        assertInstanceOf(
                ResilientRateLimiter.class,
                limiter);
    }

    @Test
    void shouldWireCircuitBreakerInsideResilienceLayer() {

        ResilientRateLimiter resilient =
                (ResilientRateLimiter)
                        registry.getLimiter(
                                Algorithm.TOKEN_BUCKET);

        assertInstanceOf(
                CircuitBreakerRateLimiter.class,
                resilient.getDelegate());
    }
}