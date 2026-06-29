package com.rateLimiter.distributedratelimiter.policy;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.key.IpKeyExtractor;
import com.rateLimiter.distributedratelimiter.key.KeyExtractor;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitPolicyTest {

    @Test
    void shouldCreatePolicySuccessfully() {

        KeyExtractor extractor =
                new IpKeyExtractor();

        RateLimitRule rule =
                new RateLimitRule(
                        "demo",
                        10,
                        Duration.ofSeconds(60),
                        Algorithm.TOKEN_BUCKET);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        extractor,
                        rule);

        assertEquals(
                extractor,
                policy.extractor());

        assertEquals(
                rule,
                policy.rule());
    }

    @Test
    void shouldThrowExceptionWhenExtractorIsNull() {

        RateLimitRule rule =
                new RateLimitRule(
                        "demo",
                        10,
                        Duration.ofSeconds(60),
                        Algorithm.TOKEN_BUCKET);

        assertThrows(
                NullPointerException.class,
                () -> new RateLimitPolicy(
                        null,
                        rule));
    }

    @Test
    void shouldThrowExceptionWhenRuleIsNull() {

        KeyExtractor extractor =
                new IpKeyExtractor();

        assertThrows(
                NullPointerException.class,
                () -> new RateLimitPolicy(
                        extractor,
                        null));
    }
}