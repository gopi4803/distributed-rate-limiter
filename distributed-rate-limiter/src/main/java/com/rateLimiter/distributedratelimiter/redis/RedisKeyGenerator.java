package com.rateLimiter.distributedratelimiter.redis;

import java.util.Objects;

public final class RedisKeyGenerator {

    private static final String PREFIX="ratelimit";

    private RedisKeyGenerator(){}

    public static String generateBucketKey(String key){
        Objects.requireNonNull(key,"Key must be non null");
        return PREFIX+":bucket:"+key;
    }

    public static String generateWindowKey(String key){
        Objects.requireNonNull(key,"Key must be non null");
        return PREFIX+":window:"+key;
    }
}
