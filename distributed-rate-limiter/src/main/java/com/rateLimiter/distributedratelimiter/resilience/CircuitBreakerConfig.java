package com.rateLimiter.distributedratelimiter.resilience;

import com.rateLimiter.distributedratelimiter.exceptions.CircuitBreakerException;

import java.time.Duration;
import java.util.Objects;

public record CircuitBreakerConfig(int failureThreshold, Duration waitDurationInOpenState) {

    public CircuitBreakerConfig{
        if(failureThreshold<=0){
            throw new CircuitBreakerException("Failure threshold must be greater than zero");
        }
        Objects.requireNonNull(waitDurationInOpenState,"Wait duration must be non null");
        if(waitDurationInOpenState.isZero() || waitDurationInOpenState.isNegative()) {
            throw new CircuitBreakerException("Wait duration must be greater than zero");
        }
    }

}
