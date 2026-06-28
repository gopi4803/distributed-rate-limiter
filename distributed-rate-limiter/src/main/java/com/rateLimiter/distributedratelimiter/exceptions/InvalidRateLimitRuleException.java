package com.rateLimiter.distributedratelimiter.exceptions;

public class InvalidRateLimitRuleException extends RateLimiterException{

    public InvalidRateLimitRuleException(String message){
        super(message);
    }

}
