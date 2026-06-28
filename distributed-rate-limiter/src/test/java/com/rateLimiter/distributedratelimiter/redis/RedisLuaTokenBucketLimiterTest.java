package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
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
class RedisLuaTokenBucketLimiterTest {

    @Container
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    private RedisLuaTokenBucketLimiter limiter;

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

        template.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();

        @SuppressWarnings({"rawtypes"})
        DefaultRedisScript script =
                new DefaultRedisScript();

        script.setLocation(
                new ClassPathResource(
                        "scripts/token_bucket.lua"));

        script.setResultType(List.class);

        limiter =
                new RedisLuaTokenBucketLimiter(
                        template,
                        script);
    }

    @Test
    void shouldAllowExactlyLimitRequests() {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        5,
                        Duration.ofSeconds(10),
                        Algorithm.TOKEN_BUCKET);

        for (int i = 0; i < 5; i++) {

            RateLimitResult result =
                    limiter.tryAcquire(
                            "user-1",
                            rule);

            assertTrue(result.allowed());
        }

        assertFalse(
                limiter.tryAcquire(
                        "user-1",
                        rule).allowed());
    }

    @Test
    void shouldKeepBucketsIndependentAcrossKeys() {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        1,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET);

        assertTrue(
                limiter.tryAcquire(
                        "user-A",
                        rule).allowed());

        assertTrue(
                limiter.tryAcquire(
                        "user-B",
                        rule).allowed());

        assertFalse(
                limiter.tryAcquire(
                        "user-A",
                        rule).allowed());
    }

    @Test
    void shouldReturnRetryAfterWhenBlocked() {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        1,
                        Duration.ofSeconds(10),
                        Algorithm.TOKEN_BUCKET);

        limiter.tryAcquire(
                "retry-user",
                rule);

        RateLimitResult result =
                limiter.tryAcquire(
                        "retry-user",
                        rule);

        assertFalse(result.allowed());

        assertTrue(
                result.retryAfterMs() > 0);
    }

    @Test
    void shouldRefillTokensAfterTimePasses()
            throws InterruptedException {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        5,
                        Duration.ofSeconds(5),
                        Algorithm.TOKEN_BUCKET);

        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire(
                    "refill-user",
                    rule);
        }

        assertFalse(
                limiter.tryAcquire(
                        "refill-user",
                        rule).allowed());

        Thread.sleep(1500);

        assertTrue(
                limiter.tryAcquire(
                        "refill-user",
                        rule).allowed());
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
                            "shared-key",
                            rule).allowed()) {

                        allowedRequests.incrementAndGet();
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                } finally {

                    completionLatch.countDown();
                }
            });
        }

        assertTrue(
                readyLatch.await(
                        5,
                        TimeUnit.SECONDS));

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

    @Test
    void shouldNeverExceedBucketCapacity()
            throws InterruptedException {

        RateLimitRule rule =
                new RateLimitRule(
                        "test",
                        10,
                        Duration.ofSeconds(10),
                        Algorithm.TOKEN_BUCKET);

        Thread.sleep(5000);

        RateLimitResult result =
                limiter.tryAcquire(
                        "capacity-user",
                        rule);

        assertTrue(result.allowed());

        assertEquals(
                9,
                result.remaining());
    }
}