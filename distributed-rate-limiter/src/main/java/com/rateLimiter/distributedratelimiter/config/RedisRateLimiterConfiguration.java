package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.redis.NaiveRedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisRateLimiterConfiguration {

    @Bean
    public RateLimiter redisRateLimiter(StringRedisTemplate redisTemplate){
        return new NaiveRedisRateLimiter(redisTemplate);
    }

}
