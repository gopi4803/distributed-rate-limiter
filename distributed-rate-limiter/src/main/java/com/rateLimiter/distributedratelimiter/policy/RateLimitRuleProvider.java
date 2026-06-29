package com.rateLimiter.distributedratelimiter.policy;

import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;

public interface RateLimitRuleProvider {
    RateLimitRule getRule(String ruleName);
}
