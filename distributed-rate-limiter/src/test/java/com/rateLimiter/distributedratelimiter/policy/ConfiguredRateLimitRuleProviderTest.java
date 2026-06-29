package com.rateLimiter.distributedratelimiter.policy;

import com.rateLimiter.distributedratelimiter.config.RateLimiterProperties;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.RateLimiterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguredRateLimitRuleProviderTest {

    private ConfiguredRateLimitRuleProvider provider;

    @BeforeEach
    void setUp() {

        RateLimiterProperties properties =
                new RateLimiterProperties();

        RateLimiterProperties.RuleProperties demoRule =
                new RateLimiterProperties.RuleProperties();

        demoRule.setLimit(5);
        demoRule.setWindow(Duration.ofSeconds(10));
        demoRule.setAlgorithm(Algorithm.TOKEN_BUCKET);

        Map<String, RateLimiterProperties.RuleProperties> rules =
                new HashMap<>();

        rules.put("demo", demoRule);

        properties.setRules(rules);

        provider =
                new ConfiguredRateLimitRuleProvider(properties);

        provider.initialize();
    }

    @Test
    void shouldReturnConfiguredRule() {

        RateLimitRule rule =
                provider.getRule("demo");

        assertEquals("demo", rule.name());
        assertEquals(5, rule.limit());
        assertEquals(Duration.ofSeconds(10), rule.window());
        assertEquals(
                Algorithm.TOKEN_BUCKET,
                rule.algorithm());
    }

    @Test
    void shouldThrowExceptionForUnknownRule() {

        RateLimiterException exception =
                assertThrows(
                        RateLimiterException.class,
                        () -> provider.getRule("unknown"));

        assertTrue(
                exception.getMessage()
                        .contains("Unknown Rate limit rule"));
    }

    @Test
    void shouldFailWhenNoRulesConfigured() {

        RateLimiterProperties properties =
                new RateLimiterProperties();

        provider =
                new ConfiguredRateLimitRuleProvider(properties);

        assertThrows(
                RateLimiterException.class,
                () -> provider.initialize());
    }
}