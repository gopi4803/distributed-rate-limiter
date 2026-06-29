package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.http.RateLimitInterceptor;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcRateLimitConfiguration
        implements WebMvcConfigurer {

    private final RateLimitInterceptor interceptor;

    public WebMvcRateLimitConfiguration(
            RateLimitInterceptor interceptor) {

        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        registry.addInterceptor(interceptor)
                .excludePathPatterns("/actuator/**");
    }
}