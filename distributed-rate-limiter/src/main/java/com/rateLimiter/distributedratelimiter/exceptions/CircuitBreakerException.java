package com.rateLimiter.distributedratelimiter.exceptions;

public class CircuitBreakerException extends RateLimiterException{

    public CircuitBreakerException(String message){
        super(message);
    }
}
