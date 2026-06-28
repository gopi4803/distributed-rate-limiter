package com.rateLimiter.distributedratelimiter.exceptions;

public class CircuitBreakerOpenException extends RateLimiterException{

    public CircuitBreakerOpenException(){
        super("Circuit Breaker is Open");
    }

}
