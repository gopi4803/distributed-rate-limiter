package com.rateLimiter.distributedratelimiter.core.registry;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.algorithm.SlidingWindowCounterLimiter;
import com.rateLimiter.distributedratelimiter.core.algorithm.TokenBucketLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.MutableClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterRegistryTest {

    private RateLimiterRegistry registry;
    private RateLimiter tokenBucketLimiter;
    private RateLimiter slidingWindowLimiter;

    @BeforeEach
    void setUp(){
        MutableClockProvider clock=new MutableClockProvider(0);
        tokenBucketLimiter=new TokenBucketLimiter(clock);
        slidingWindowLimiter=new SlidingWindowCounterLimiter(clock);
        registry=new RateLimiterRegistry(Map.of(Algorithm.TOKEN_BUCKET,tokenBucketLimiter,Algorithm.SLIDING_WINDOW_COUNTER,slidingWindowLimiter));
    }

    @Test
    void shouldReturnTokenBucketLimiter(){
        RateLimiter limiter=registry.getLimiter(Algorithm.TOKEN_BUCKET);
        assertSame(tokenBucketLimiter,limiter);
    }

    @Test
    void shouldReturnSlidingWindowLimiter(){
        RateLimiter limiter=registry.getLimiter(Algorithm.SLIDING_WINDOW_COUNTER);
        assertSame(slidingWindowLimiter,limiter);
    }

    @Test
    void shouldRejectNullAlgorithm(){
        assertThrows(NullPointerException.class,()->registry.getLimiter(null));
    }

    @Test
    void shouldRejectEmptyRegistry() {
        assertThrows(IllegalArgumentException.class,() -> new RateLimiterRegistry(Map.of()));
    }

    @Test
    void shouldCreateImmutableRegistrySnapshot(){
        MutableClockProvider clock=new MutableClockProvider(0);
        Map<Algorithm,RateLimiter> limiters=new EnumMap<>(Algorithm.class);
        limiters.put(Algorithm.TOKEN_BUCKET,new TokenBucketLimiter(clock));
        RateLimiterRegistry registry=new RateLimiterRegistry(limiters);
        limiters.clear();
        assertDoesNotThrow(()->registry.getLimiter(Algorithm.TOKEN_BUCKET));
    }

    @Test
    void shouldRejectNullLimiterMap(){
        assertThrows(NullPointerException.class,()->new RateLimiterRegistry(null));
    }

    @Test
    void shouldRejectMapContainingNullKeys(){
        Map<Algorithm,RateLimiter> invalidMap=new HashMap<>();
        invalidMap.put(null,tokenBucketLimiter);
        assertThrows(NullPointerException.class,()->new RateLimiterRegistry(invalidMap));
    }

    @Test
    void shouldRejectMapContainingNullValues(){
        Map<Algorithm,RateLimiter> invalidMap=new HashMap<>();
        invalidMap.put(Algorithm.TOKEN_BUCKET,null);
        assertThrows(NullPointerException.class,()->new RateLimiterRegistry(invalidMap));
    }

    @Test
    void shouldThrowExceptionWhenRequestedAlgorithmIsNotRegistered() {
        MutableClockProvider clock = new MutableClockProvider();
        RateLimiterRegistry registry = new RateLimiterRegistry(
                                Map.of(
                                Algorithm.TOKEN_BUCKET,
                                new TokenBucketLimiter(clock)));

        assertThrows(IllegalArgumentException.class, () -> registry.getLimiter(Algorithm.SLIDING_WINDOW_COUNTER));
    }
}
