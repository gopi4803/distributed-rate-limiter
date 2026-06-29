package com.rateLimiter.distributedratelimiter.controller;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
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

    @GetMapping("/{algorithm}")
    public ResponseEntity<String> testRateLimiter(@PathVariable Algorithm algorithm, @RequestParam(defaultValue = "demo-user") String key){
        RateLimiter limiter=registry.getLimiter(algorithm);
        RateLimitRule rule=new RateLimitRule("demo-rule",5, Duration.ofSeconds(10),algorithm);
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
