package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaFixedWindowLimiter;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaSlidingWindowLimiter;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaTokenBucketLimiter;
import com.rateLimiter.distributedratelimiter.resilience.CircuitBreakerConfig;
import com.rateLimiter.distributedratelimiter.resilience.CircuitBreakerRateLimiter;
import com.rateLimiter.distributedratelimiter.resilience.FailureStrategy;
import com.rateLimiter.distributedratelimiter.resilience.ResilientRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RateLimiterRegistryConfiguration {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(
            StringRedisTemplate redisTemplate,
            RedisScript<List<Long>> tokenBucketLuaScript,
            RedisScript<List<Long>> slidingWindowLuaScript,
            RedisScript<List<Long>> fixedWindowLuaScript,
            ClockProvider clockProvider) {

        Map<Algorithm, RateLimiter> limiters = new EnumMap<>(Algorithm.class);

        limiters.put(Algorithm.TOKEN_BUCKET,
                buildResilientLimiter(
                        new RedisLuaTokenBucketLimiter(
                                redisTemplate,
                                tokenBucketLuaScript),
                        clockProvider));

        limiters.put(Algorithm.SLIDING_WINDOW_COUNTER,
                buildResilientLimiter(
                        new RedisLuaSlidingWindowLimiter(
                                redisTemplate,
                                slidingWindowLuaScript),
                        clockProvider));

        limiters.put(Algorithm.FIXED_WINDOW,
                buildResilientLimiter(
                        new RedisLuaFixedWindowLimiter(
                                redisTemplate,
                                fixedWindowLuaScript),
                        clockProvider));

        return new RateLimiterRegistry(limiters);
    }

    private RateLimiter buildResilientLimiter(
            RateLimiter delegate,
            ClockProvider clockProvider) {

        return new CircuitBreakerRateLimiter(
                new ResilientRateLimiter(
                        delegate,
                        FailureStrategy.FAIL_OPEN),
                new CircuitBreakerConfig(
                        5,
                        Duration.ofSeconds(30)),
                clockProvider);
    }
}