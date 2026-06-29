package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestConfiguration.class)
                    .withPropertyValues(
                            "rate-limiter.rules.demo.limit=5",
                            "rate-limiter.rules.demo.window=10s",
                            "rate-limiter.rules.demo.algorithm=TOKEN_BUCKET"
                    );

    @Test
    void shouldBindPropertiesCorrectly() {

        contextRunner.run(context -> {

            RateLimiterProperties properties =
                    context.getBean(RateLimiterProperties.class);

            var demoRule =
                    properties.getRules().get("demo");

            assertThat(demoRule).isNotNull();

            assertThat(demoRule.getLimit())
                    .isEqualTo(5);

            assertThat(demoRule.getWindow())
                    .isEqualTo(Duration.ofSeconds(10));

            assertThat(demoRule.getAlgorithm())
                    .isEqualTo(Algorithm.TOKEN_BUCKET);
        });
    }

    @Configuration
    @EnableConfigurationProperties(RateLimiterProperties.class)
    static class TestConfiguration {
    }
}