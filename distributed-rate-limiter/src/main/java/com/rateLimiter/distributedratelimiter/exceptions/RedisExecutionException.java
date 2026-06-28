package com.rateLimiter.distributedratelimiter.exceptions;

public class RedisExecutionException extends RateLimiterException{

    public RedisExecutionException(String message,Throwable cause){
        super(message,cause);
    }
}
