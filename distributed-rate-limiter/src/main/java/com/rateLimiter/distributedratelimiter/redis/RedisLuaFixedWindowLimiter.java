package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.utils.ValidationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

public class RedisLuaFixedWindowLimiter extends AbstractRedisLuaRateLimiter {

    public RedisLuaFixedWindowLimiter(
            StringRedisTemplate redisTemplate,
            RedisScript<List<Long>> script) {
        super(redisTemplate, script);
    }

    @Override
    public RateLimitResult tryAcquire(
            String key,
            RateLimitRule rule) {
        ValidationUtils.validateInputs(key, rule);
        String redisKey = RedisKeyGenerator.generateBucketKey(key);
        List<Long> result = executeScript(
                        List.of(redisKey),
                        String.valueOf(rule.limit()),
                        String.valueOf(rule.window().toMillis()));
        return new RateLimitResult(
                result.get(0) == 1L,
                result.get(1),
                result.get(2));
    }
}