package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.redis.RedisLuaFixedWindowLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RedisLuaRateLimiterConfiguration {

    @Bean
    public RateLimiter redisLuaRateLimiter(StringRedisTemplate redisTemplate, RedisScript<List<Long>> fixedWindowLuaScript){
        return new RedisLuaFixedWindowLimiter(redisTemplate,fixedWindowLuaScript);
    }

}
