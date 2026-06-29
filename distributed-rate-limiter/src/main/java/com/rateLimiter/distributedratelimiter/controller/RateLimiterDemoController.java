package com.rateLimiter.distributedratelimiter.controller;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/rate-limit")
@RequiredArgsConstructor
public class RateLimiterDemoController {

    private final RateLimiterRegistry registry;
    private final RateLimitRuleProvider ruleProvider;

    @GetMapping("/{ruleName}")
    public ResponseEntity<String> testRateLimiter(@PathVariable String ruleName, @RequestParam(defaultValue = "demo-user") String key){
        RateLimitRule rule=ruleProvider.getRule(ruleName);
        RateLimiter limiter=registry.getLimiter(rule.algorithm());
        RateLimitResult result=limiter.tryAcquire(key,rule);
        if(!result.allowed()){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Retry after "+result.retryAfterMs()+" ms");
        }
        return ResponseEntity.ok(
                "Allowed=" + result.allowed()
                        + ", Remaining=" + result.remaining()
                        + ", RetryAfter=" + result.retryAfterMs());
    }

}
