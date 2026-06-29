package com.rateLimiter.distributedratelimiter.http;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.key.IpKeyExtractor;
import com.rateLimiter.distributedratelimiter.key.KeyExtractor;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimiterRegistry registry;
    private RateLimiter limiter;
    private RateLimiterMetrics metrics;

    private RateLimitFilter filter;

    private RateLimitRule rule;

    @BeforeEach
    void setUp() {

        registry = mock(RateLimiterRegistry.class);
        limiter = mock(RateLimiter.class);
        metrics = mock(RateLimiterMetrics.class);

        KeyExtractor extractor = new IpKeyExtractor();

        rule = new RateLimitRule(
                "demo",
                5,
                Duration.ofSeconds(10),
                Algorithm.TOKEN_BUCKET);

        RateLimitPolicy policy =
                new RateLimitPolicy(extractor, rule);

        filter = new RateLimitFilter(
                registry,
                List.of(policy),
                metrics);

        when(registry.getLimiter(Algorithm.TOKEN_BUCKET))
                .thenReturn(limiter);
    }

    @Test
    void shouldContinueFilterChainWhenRequestAllowed()
            throws Exception {

        when(limiter.tryAcquire(any(), any()))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                4,
                                0));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                spy(new MockFilterChain());

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(limiter)
                .tryAcquire(
                        eq("ip:127.0.0.1:/api/test"),
                        eq(rule));

        verify(metrics)
                .recordAllowed(Algorithm.TOKEN_BUCKET);

        assertEquals(200, response.getStatus());

        assertEquals(
                "5",
                response.getHeader(
                        "X-RateLimit-Limit"));

        assertEquals(
                "4",
                response.getHeader(
                        "X-RateLimit-Remaining"));

        assertEquals(
                "TOKEN_BUCKET",
                response.getHeader(
                        "X-RateLimit-Algorithm"));
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded()
            throws Exception {

        when(limiter.tryAcquire(any(), any()))
                .thenReturn(
                        new RateLimitResult(
                                false,
                                0,
                                5000));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                spy(new MockFilterChain());

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(metrics)
                .recordBlocked(Algorithm.TOKEN_BUCKET);

        assertEquals(
                429,
                response.getStatus());

        assertEquals(
                "ip",
                response.getHeader(
                        "X-RateLimit-LimitedBy"));

        assertNotNull(
                response.getHeader(
                        "Retry-After"));
    }

    @Test
    void shouldSkipActuatorEndpoints()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/actuator/health");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                filterChain);

        verifyNoInteractions(limiter);
    }

    @Test
    void shouldContinueWhenExtractorReturnsNull()
            throws Exception {

        KeyExtractor extractor = mock(KeyExtractor.class);

        when(extractor.extract(any()))
                .thenReturn(null);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        extractor,
                        rule);

        filter = new RateLimitFilter(
                registry,
                List.of(policy),
                metrics);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                filterChain);

        verifyNoInteractions(limiter);
    }

    @Test
    void shouldGenerateEndpointSpecificKeys()
            throws Exception {

        when(limiter.tryAcquire(any(), any()))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                4,
                                0));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(limiter).tryAcquire(
                eq("ip:127.0.0.1:/api/test"),
                eq(rule));

        verify(metrics)
                .recordAllowed(Algorithm.TOKEN_BUCKET);
    }

    @Test
    void shouldExposeRateLimitHeadersWhenRequestBlocked()
            throws Exception {

        when(limiter.tryAcquire(any(), any()))
                .thenReturn(
                        new RateLimitResult(
                                false,
                                0,
                                5000));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                filterChain);

        assertEquals(
                "5",
                response.getHeader(
                        "X-RateLimit-Limit"));

        assertEquals(
                "0",
                response.getHeader(
                        "X-RateLimit-Remaining"));

        assertEquals(
                "TOKEN_BUCKET",
                response.getHeader(
                        "X-RateLimit-Algorithm"));

        assertNotNull(
                response.getHeader(
                        "Retry-After"));
    }
}