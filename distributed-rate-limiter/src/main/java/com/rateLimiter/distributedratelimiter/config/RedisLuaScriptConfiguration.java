package com.rateLimiter.distributedratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RedisLuaScriptConfiguration {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RedisScript<List<Long>> fixedWindowLuaScript() {
        DefaultRedisScript script = new DefaultRedisScript();
        script.setLocation(new ClassPathResource("scripts/fixed_window.lua"));
        script.setResultType(List.class);
        return script;
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RedisScript<List<Long>> tokenBucketLuaScript() {
        DefaultRedisScript script = new DefaultRedisScript();
        script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        script.setResultType(List.class);
        return script;
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RedisScript<List<Long>> slidingWindowLuaScript() {
        DefaultRedisScript script = new DefaultRedisScript();
        script.setLocation(new ClassPathResource("scripts/sliding_window_counter.lua"));
        script.setResultType(List.class);
        return script;
    }
}