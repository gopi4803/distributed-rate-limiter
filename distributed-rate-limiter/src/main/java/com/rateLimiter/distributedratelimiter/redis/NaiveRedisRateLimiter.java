package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.utils.ValidationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NaiveRedisRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public NaiveRedisRateLimiter(StringRedisTemplate redisTemplate){
        this.redisTemplate= Objects.requireNonNull(redisTemplate,"RedisTemplate must be non null");
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule){
        ValidationUtils.validateInputs(key,rule);
        String redisKey=RedisKeyGenerator.generateBucketKey(key);
        String currentCountString=redisTemplate.opsForValue().get(redisKey);
        long currentCount=currentCountString==null?0:Long.parseLong(currentCountString);
        if(currentCount>=rule.limit()){
            Long ttlSeconds=redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            long retryAfterMs=(ttlSeconds==null || ttlSeconds<0)? rule.window().toMillis():ttlSeconds*1000;
            return new RateLimitResult(false,0,retryAfterMs);
        }
        Long updatedCount=redisTemplate.opsForValue().increment(redisKey);
        if(updatedCount!=null && updatedCount==1L){
            redisTemplate.expire(redisKey,rule.window());
        }
        long remaining=updatedCount==null?0:Math.max(rule.limit()-updatedCount,0);
        return new RateLimitResult(true,remaining,0);
    }
}
