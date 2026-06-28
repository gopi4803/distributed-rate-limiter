package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RateLimiterRegistryConfigurationTest {

    @Autowired
    private RateLimiterRegistry registry;

    @Test
    void shouldCreateRateLimitRegistryBean(){
        assertNotNull(registry);
    }

    @Test
    void shouldRegisterTokenBucketLimiter() {
        assertNotNull(registry.getLimiter(Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldRegisterSlidingWindowLimiter() {
        assertNotNull(registry.getLimiter(Algorithm.SLIDING_WINDOW_COUNTER));
    }

}
