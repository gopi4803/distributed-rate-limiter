package com.rateLimiter.distributedratelimiter.controller;

import com.rateLimiter.distributedratelimiter.annotation.RateLimited;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rate-limit")
public class RateLimiterDemoController {

    @RateLimited("demo")
    @GetMapping("/{ruleName}")
    public ResponseEntity<String> testRateLimiter(
            @PathVariable String ruleName) {

        return ResponseEntity.ok(
                "Request processed successfully");
    }
}