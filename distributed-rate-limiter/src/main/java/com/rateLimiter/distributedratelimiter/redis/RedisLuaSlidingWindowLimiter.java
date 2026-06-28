package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.utils.ValidationUtils;
import com.rateLimiter.distributedratelimiter.exceptions.InvalidLuaScriptException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Objects;

public class RedisLuaSlidingWindowLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> script;

    public RedisLuaSlidingWindowLimiter(StringRedisTemplate redisTemplate, RedisScript<List<Long>> script) {
        this.redisTemplate= Objects.requireNonNull(redisTemplate,"RedisTemplate must be non null");
        this.script=Objects.requireNonNull(script,"Script must be non null");
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule) {
        ValidationUtils.validateInputs(key, rule);
        List<Long> result =
                redisTemplate.execute(
                        script,
                        List.of(
                                RedisKeyGenerator
                                        .generateWindowKey(
                                                key)),
                        String.valueOf(
                                rule.limit()),
                        String.valueOf(
                                rule.window().toMillis()));

        if (result == null || result.size() != 3) {
            throw new InvalidLuaScriptException("Unexpected Lua response");
        }

        return new RateLimitResult(result.get(0) == 1L, result.get(1), result.get(2));
    }
}