package com.rateLimiter.distributedratelimiter.core.registry;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;

import java.util.Map;
import java.util.Objects;

public class RateLimiterRegistry {

    private final Map<Algorithm, RateLimiter> limiters;

    public RateLimiterRegistry(Map<Algorithm,RateLimiter> limiters){
        Objects.requireNonNull(limiters,"Limiters must not be null");
        if(limiters.isEmpty()) throw new IllegalArgumentException("Atleast one limiter must be registered");
        this.limiters=Map.copyOf(limiters);
    }

    public RateLimiter getLimiter(Algorithm algorithm){
        Objects.requireNonNull(algorithm,"Algorithm must be non null");
        RateLimiter limiter=limiters.get(algorithm);
        if(limiter==null){
            throw new IllegalArgumentException("No Rate Limiter for this algorithm: "+algorithm);
        }
        return limiter;
    }

}
