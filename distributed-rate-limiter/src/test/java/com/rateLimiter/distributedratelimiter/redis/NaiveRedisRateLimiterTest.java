package com.rateLimiter.distributedratelimiter.redis;

import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class NaiveRedisRateLimiterTest {

    @Container
    static GenericContainer<?> redisContainer=new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private NaiveRedisRateLimiter limiter;

    @BeforeEach
    void setUp(){
        LettuceConnectionFactory factory=new LettuceConnectionFactory(redisContainer.getHost(),redisContainer.getMappedPort(6379));
        factory.afterPropertiesSet();
        StringRedisTemplate template=new StringRedisTemplate(factory);
        limiter=new NaiveRedisRateLimiter(template);
    }

    @Test
    void shouldAllowRequestsWithinLimit(){
        RateLimitRule rule=new RateLimitRule("test",5, Duration.ofMinutes(1), Algorithm.TOKEN_BUCKET);
        for(int i=0;i<5;i++) {
            assertTrue(limiter.tryAcquire("user-1",rule).allowed());
        }
        assertFalse(limiter.tryAcquire("user-1",rule).allowed());
    }

    @Test
    void shouldDemonstrateRaceConditionUnderConcurrency() throws InterruptedException{
        RateLimitRule rule=new RateLimitRule("test",10,Duration.ofMinutes(1),Algorithm.TOKEN_BUCKET);
        int threadCount=500;
        ExecutorService executor= Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch=new CountDownLatch(threadCount);
        CountDownLatch startLatch=new CountDownLatch(1);
        CountDownLatch completionLatch=new CountDownLatch(threadCount);
        AtomicInteger allowedRequests=new AtomicInteger();
        for(int i=0;i<threadCount;i++){
            executor.submit(()->{
                try{
                    readyLatch.countDown();
                    startLatch.await();
                    if(limiter.tryAcquire("shared-key",rule).allowed()){
                        allowedRequests.incrementAndGet();
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }finally {
                    completionLatch.countDown();
                }
            });
        }
        assertTrue(readyLatch.await(5,TimeUnit.SECONDS));

        // Fire all threads simultaneously
        startLatch.countDown();
        assertTrue(completionLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        System.out.println("Allowed Requests = "+allowedRequests.get());
        assertTrue(allowedRequests.get()>rule.limit(),"Naive implementation should over-allocate requests");
    }
}
