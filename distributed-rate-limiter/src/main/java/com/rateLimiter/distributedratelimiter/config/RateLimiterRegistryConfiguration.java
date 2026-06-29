package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
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
            ClockProvider clockProvider, RateLimiterMetrics metrics) {

        Map<Algorithm, RateLimiter> limiters = new EnumMap<>(Algorithm.class);

        limiters.put(Algorithm.TOKEN_BUCKET,
                buildResilientLimiter(
                        new RedisLuaTokenBucketLimiter(
                                redisTemplate,
                                tokenBucketLuaScript),
                        clockProvider,metrics,Algorithm.TOKEN_BUCKET));

        limiters.put(Algorithm.SLIDING_WINDOW_COUNTER,
                buildResilientLimiter(
                        new RedisLuaSlidingWindowLimiter(
                                redisTemplate,
                                slidingWindowLuaScript),
                        clockProvider,metrics,Algorithm.SLIDING_WINDOW_COUNTER));

        limiters.put(Algorithm.FIXED_WINDOW,
                buildResilientLimiter(
                        new RedisLuaFixedWindowLimiter(
                                redisTemplate,
                                fixedWindowLuaScript),
                        clockProvider,metrics,Algorithm.FIXED_WINDOW));

        return new RateLimiterRegistry(limiters);
    }

    private RateLimiter buildResilientLimiter(
            RateLimiter delegate,
            ClockProvider clockProvider,RateLimiterMetrics metrics,Algorithm algorithm) {

        RateLimiter protectedLimiter =
                new CircuitBreakerRateLimiter(
                        delegate,
                        new CircuitBreakerConfig(
                                5,
                                Duration.ofSeconds(30)),
                        clockProvider,metrics,algorithm);

        return new ResilientRateLimiter(
                protectedLimiter,
                FailureStrategy.FAIL_OPEN,metrics,algorithm);
    }
}