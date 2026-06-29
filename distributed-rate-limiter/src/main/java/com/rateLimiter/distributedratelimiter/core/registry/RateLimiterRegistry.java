package com.rateLimiter.distributedratelimiter.core.registry;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.exceptions.InvalidRateLimitRuleException;
import com.rateLimiter.distributedratelimiter.exceptions.UnsupportedAlgorithmException;

import java.util.Map;
import java.util.Objects;

public class RateLimiterRegistry {

    private final Map<Algorithm, RateLimiter> limiters;

    public RateLimiterRegistry(Map<Algorithm, RateLimiter> limiters) {
        Objects.requireNonNull(limiters, "Limiters must not be null");
        if (limiters.isEmpty()) throw new InvalidRateLimitRuleException("At least one limiter must be registered");
        this.limiters = Map.copyOf(limiters);
    }

    public RateLimiter getLimiter(Algorithm algorithm) {
        Objects.requireNonNull(algorithm, "Algorithm must be non null");
        RateLimiter limiter = limiters.get(algorithm);
        if (limiter == null) {
            throw new UnsupportedAlgorithmException(algorithm);
        }
        return limiter;
    }

}
