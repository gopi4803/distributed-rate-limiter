package com.rateLimiter.distributedratelimiter.exceptions;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;

public class UnsupportedAlgorithmException extends RateLimiterException{

    public UnsupportedAlgorithmException(Algorithm algorithm){
        super("No Rate Limiter registered for algorithm: "+algorithm);
    }
}
