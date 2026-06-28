package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaSlidingWindowLimiter;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaTokenBucketLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void shouldRegisterTokenBucketLimiter(){
        RateLimiter limiter=registry.getLimiter(Algorithm.TOKEN_BUCKET);
        assertNotNull(limiter);
        assertInstanceOf(RedisLuaTokenBucketLimiter.class,limiter);
    }

    @Test
    void shouldRegisterSlidingWindowLimiter(){
        RateLimiter limiter=registry.getLimiter(Algorithm.SLIDING_WINDOW_COUNTER);
        assertNotNull(limiter);
        assertInstanceOf(RedisLuaSlidingWindowLimiter.class,limiter);
    }

}
