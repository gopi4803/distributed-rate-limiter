package com.rateLimiter.distributedratelimiter.key;

import jakarta.servlet.http.HttpServletRequest;

public interface KeyExtractor {
    String extract(HttpServletRequest request);
    String prefix();
}
