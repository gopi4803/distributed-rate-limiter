package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisLuaFixedWindowLimiterTest {

    @Container
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    private RedisLuaFixedWindowLimiter limiter;
    private StringRedisTemplate template;

    @BeforeEach
    void setUp() {

        LettuceConnectionFactory factory =
                new LettuceConnectionFactory(
                        redisContainer.getHost(),
                        redisContainer.getMappedPort(6379));

        factory.afterPropertiesSet();

        template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();

        // Ensure complete isolation between tests
        template.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();

        @SuppressWarnings({"rawtypes"})
        DefaultRedisScript script =
                new DefaultRedisScript();

        script.setLocation(
                new ClassPathResource(
                        "scripts/fixed_window.lua"));

        script.setResultType(List.class);

        limiter =
                new RedisLuaFixedWindowLimiter(
                        template,
                        script);
    }

    @Test
    void shouldAllowRequestsWithinLimit() {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        5,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET);

        for (int i = 0; i < 5; i++) {

            assertTrue(
                    limiter.tryAcquire(
                            "allow-test-user",
                            rule).allowed());
        }

        assertFalse(
                limiter.tryAcquire(
                        "allow-test-user",
                        rule).allowed());
    }

    @Test
    void shouldReturnRetryAfterWhenRequestIsBlocked() {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        1,
                        Duration.ofSeconds(30),
                        Algorithm.TOKEN_BUCKET);

        assertTrue(
                limiter.tryAcquire(
                        "retry-user",
                        rule).allowed());

        var result =
                limiter.tryAcquire(
                        "retry-user",
                        rule);

        assertFalse(result.allowed());

        assertTrue(result.retryAfterMs() > 0);
    }

    @Test
    void shouldNeverExceedLimitUnderConcurrency()
            throws InterruptedException {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        10,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET);

        int threadCount = 500;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch =
                new CountDownLatch(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch completionLatch =
                new CountDownLatch(threadCount);

        AtomicInteger allowedRequests =
                new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {

            executor.submit(() -> {

                try {

                    readyLatch.countDown();

                    startLatch.await();

                    if (limiter.tryAcquire(
                            "shared-concurrency-key",
                            rule).allowed()) {

                        allowedRequests.incrementAndGet();
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    completionLatch.countDown();
                }
            });
        }

        assertTrue(
                readyLatch.await(
                        5,
                        TimeUnit.SECONDS));

        // Start all threads simultaneously
        startLatch.countDown();

        assertTrue(
                completionLatch.await(
                        15,
                        TimeUnit.SECONDS));

        executor.shutdown();

        System.out.println(
                "Allowed Requests = "
                        + allowedRequests.get());

        assertEquals(
                rule.limit(),
                allowedRequests.get());
    }
}