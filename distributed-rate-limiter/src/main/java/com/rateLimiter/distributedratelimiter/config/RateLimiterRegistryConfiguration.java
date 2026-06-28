package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaSlidingWindowLimiter;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaTokenBucketLimiter;
import com.rateLimiter.distributedratelimiter.resilience.FailureStrategy;
import com.rateLimiter.distributedratelimiter.resilience.ResilientRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RateLimiterRegistryConfiguration {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(StringRedisTemplate redisTemplate, RedisScript<List<Long>> tokenBucketLuaScript,RedisScript<List<Long>> slidingWindowLuaScript){
        Map<Algorithm, RateLimiter> limiters=new EnumMap<>(Algorithm.class);
        limiters.put(Algorithm.TOKEN_BUCKET,new ResilientRateLimiter(new RedisLuaTokenBucketLimiter(redisTemplate,tokenBucketLuaScript),FailureStrategy.FAIL_OPEN));
        limiters.put(Algorithm.SLIDING_WINDOW_COUNTER,new ResilientRateLimiter(new RedisLuaSlidingWindowLimiter(redisTemplate,slidingWindowLuaScript),FailureStrategy.FAIL_OPEN));
        return new RateLimiterRegistry(limiters);
    }
}
