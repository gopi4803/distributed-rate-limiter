package com.rateLimiter.distributedratelimiter.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RediKeyGeneratorTest {

    @Test
    void shouldGenerateBucketKey(){
        String key=RedisKeyGenerator.generateBucketKey("user-1");
        assertEquals("ratelimit:bucket:user-1",key);
    }

    @Test
    void shouldGenerateWindowKey(){
        String key=RedisKeyGenerator.generateWindowKey("user-1");
        assertEquals("ratelimit:window:user-1",key);
    }

    @Test
    void shouldRejectNullKeyForBucketKeyGeneration(){
        assertThrows(NullPointerException.class,()->RedisKeyGenerator.generateBucketKey(null));
    }

    @Test
    void shouldRejectNullKeyForWindowKeyGeneration(){
        assertThrows(NullPointerException.class,()->RedisKeyGenerator.generateWindowKey(null));
    }
}
