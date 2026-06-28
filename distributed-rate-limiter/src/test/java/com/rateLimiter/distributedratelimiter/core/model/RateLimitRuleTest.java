package com.rateLimiter.distributedratelimiter.core.model;

import com.rateLimiter.distributedratelimiter.exceptions.InvalidRateLimitRuleException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitRuleTest {

    @Test
    void shouldCreateValidRateLimitRule() {

        RateLimitRule rule = new RateLimitRule(
                "premium",
                100,
                Duration.ofMinutes(1),
                Algorithm.TOKEN_BUCKET
        );

        assertEquals("premium", rule.name());
        assertEquals(100, rule.limit());
        assertEquals(Duration.ofMinutes(1), rule.window());
        assertEquals(Algorithm.TOKEN_BUCKET, rule.algorithm());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RateLimitRule(
                        null,
                        100,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule name must be non null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenWindowIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RateLimitRule(
                        "premium",
                        100,
                        null,
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule window must be non null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenAlgorithmIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RateLimitRule(
                        "premium",
                        100,
                        Duration.ofMinutes(1),
                        null
                )
        );

        assertEquals(
                "Rate limit rule algorithm must be non null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> new RateLimitRule(
                        " ",
                        100,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule name must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenLimitIsZero() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> new RateLimitRule(
                        "premium",
                        0,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule limit must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenLimitIsNegative() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> new RateLimitRule(
                        "premium",
                        -1,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule limit must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenWindowIsZero() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> new RateLimitRule(
                        "premium",
                        100,
                        Duration.ZERO,
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule window must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenWindowIsNegative() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> new RateLimitRule(
                        "premium",
                        100,
                        Duration.ofSeconds(-1),
                        Algorithm.TOKEN_BUCKET
                )
        );

        assertEquals(
                "Rate limit rule window must be greater than zero",
                exception.getMessage()
        );
    }
}