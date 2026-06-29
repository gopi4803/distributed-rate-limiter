package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private Map<String,RuleProperties> rules=new HashMap<>();

    @Getter
    @Setter
    public static class RuleProperties{
        private int limit;
        private Duration window;
        private Algorithm algorithm;
    }
}
