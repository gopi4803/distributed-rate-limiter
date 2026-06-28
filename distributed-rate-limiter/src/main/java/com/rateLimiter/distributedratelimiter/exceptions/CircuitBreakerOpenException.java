package com.rateLimiter.distributedratelimiter.exceptions;

public class CircuitBreakerOpenException extends RuntimeException{

    public CircuitBreakerOpenException(){
        super("Circuit Breaker is Open");
    }

}
