package com.rateLimiter.distributedratelimiter.core.utils;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.InvalidRateLimitRuleException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    private final RateLimitRule validRule =
            new RateLimitRule(
                    "premium",
                    100,
                    Duration.ofMinutes(1),
                    Algorithm.TOKEN_BUCKET
            );

    @Test
    void shouldValidateValidInputs() {

        assertDoesNotThrow(() ->
                ValidationUtils.validateInputs(
                        "user-123",
                        validRule
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenKeyIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ValidationUtils.validateInputs(
                        null,
                        validRule
                )
        );

        assertEquals(
                "Key must not be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenRuleIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ValidationUtils.validateInputs(
                        "user-123",
                        null
                )
        );

        assertEquals(
                "Rule must not be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenKeyIsBlank() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> ValidationUtils.validateInputs(
                        " ",
                        validRule
                )
        );

        assertEquals(
                "Key must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenKeyIsEmpty() {

        InvalidRateLimitRuleException exception = assertThrows(
                InvalidRateLimitRuleException.class,
                () -> ValidationUtils.validateInputs(
                        "",
                        validRule
                )
        );

        assertEquals(
                "Key must not be blank",
                exception.getMessage()
        );
    }
}