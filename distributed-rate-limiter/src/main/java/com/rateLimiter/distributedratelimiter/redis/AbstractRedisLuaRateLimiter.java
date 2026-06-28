package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.exceptions.RedisExecutionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Objects;

public abstract class AbstractRedisLuaRateLimiter implements RateLimiter {

    protected final StringRedisTemplate redisTemplate;
    protected final RedisScript<List<Long>> script;

    protected AbstractRedisLuaRateLimiter(
            StringRedisTemplate redisTemplate,
            RedisScript<List<Long>> script) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "Redis template must not be null");
        this.script = Objects.requireNonNull(script, "Redis script must not be null");
    }

    protected List<Long> executeScript(
            List<String> keys,
            String... args) {
        try {
            List<Long> result = redisTemplate.execute(script,keys,(Object[]) args);
            if (result == null || result.isEmpty()) {
                throw new RedisExecutionException("Lua script returned null or empty result", null);
            }
            return result;
        } catch (Exception exception) {
            throw new RedisExecutionException("Failed to execute Redis Lua script", exception);
        }
    }
}