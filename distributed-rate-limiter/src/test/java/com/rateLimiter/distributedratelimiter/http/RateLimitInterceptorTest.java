package com.rateLimiter.distributedratelimiter.http;

import com.rateLimiter.distributedratelimiter.annotation.RateLimited;
import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.Algorithm;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitInterceptorTest {

    private RateLimiterRegistry registry;
    private RateLimitRuleProvider ruleProvider;
    private RateLimiterMetrics metrics;
    private RateLimiter limiter;

    private RateLimitInterceptor interceptor;

    private RateLimitRule rule;

    @BeforeEach
    void setUp() {

        registry = mock(RateLimiterRegistry.class);
        ruleProvider = mock(RateLimitRuleProvider.class);
        metrics = mock(RateLimiterMetrics.class);
        limiter = mock(RateLimiter.class);

        interceptor = new RateLimitInterceptor(
                registry,
                ruleProvider,
                metrics);

        rule = new RateLimitRule(
                "demo",
                5,
                Duration.ofSeconds(10),
                Algorithm.TOKEN_BUCKET);

        when(ruleProvider.getRule("demo"))
                .thenReturn(rule);

        when(registry.getLimiter(Algorithm.TOKEN_BUCKET))
                .thenReturn(limiter);
    }

    @Test
    void shouldAllowRequestForAnnotatedEndpoint()
            throws Exception {

        when(limiter.tryAcquire(anyString(), eq(rule)))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                4,
                                0));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("127.0.0.1");
        request.setRequestURI("/demo");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        HandlerMethod handler =
                handlerMethod("annotatedEndpoint");

        boolean result =
                interceptor.preHandle(
                        request,
                        response,
                        handler);

        assertTrue(result);

        verify(limiter)
                .tryAcquire(
                        eq("127.0.0.1:/demo"),
                        eq(rule));

        verify(metrics)
                .recordAllowed(
                        Algorithm.TOKEN_BUCKET);
    }

    @Test
    void shouldRejectRequestWhenLimitExceeded()
            throws Exception {

        when(limiter.tryAcquire(anyString(), eq(rule)))
                .thenReturn(
                        new RateLimitResult(
                                false,
                                0,
                                5000));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("127.0.0.1");
        request.setRequestURI("/demo");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        HandlerMethod handler =
                handlerMethod("annotatedEndpoint");

        boolean result =
                interceptor.preHandle(
                        request,
                        response,
                        handler);

        assertFalse(result);

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                response.getStatus());

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

        verify(metrics)
                .recordBlocked(
                        Algorithm.TOKEN_BUCKET);
    }

    @Test
    void shouldAllowRequestWhenEndpointIsNotAnnotated()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        HandlerMethod handler =
                handlerMethod("nonAnnotatedEndpoint");

        boolean result =
                interceptor.preHandle(
                        request,
                        response,
                        handler);

        assertTrue(result);

        verifyNoInteractions(limiter);
        verifyNoInteractions(metrics);
    }

    @Test
    void shouldAllowNonHandlerMethodObjects()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        Object handler = new Object();

        boolean result =
                interceptor.preHandle(
                        request,
                        response,
                        handler);

        assertTrue(result);

        verifyNoInteractions(limiter);
    }

    @Test
    void shouldApplyMultipleRules()
            throws Exception {

        RateLimitRule paymentsRule =
                new RateLimitRule(
                        "payments",
                        10,
                        Duration.ofMinutes(1),
                        Algorithm.TOKEN_BUCKET);

        when(ruleProvider.getRule("payments"))
                .thenReturn(paymentsRule);

        when(limiter.tryAcquire(anyString(), any()))
                .thenReturn(
                        new RateLimitResult(
                                true,
                                5,
                                0));

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("127.0.0.1");
        request.setRequestURI("/multi");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        HandlerMethod handler =
                handlerMethod("multipleRulesEndpoint");

        boolean result =
                interceptor.preHandle(
                        request,
                        response,
                        handler);

        assertTrue(result);

        verify(ruleProvider)
                .getRule("demo");

        verify(ruleProvider)
                .getRule("payments");

        verify(limiter, times(2))
                .tryAcquire(anyString(), any());
    }

    private HandlerMethod handlerMethod(String methodName)
            throws NoSuchMethodException {

        Method method =
                TestController.class
                        .getMethod(methodName);

        return new HandlerMethod(
                new TestController(),
                method);
    }

    static class TestController {

        @RateLimited("demo")
        public void annotatedEndpoint() {
        }

        public void nonAnnotatedEndpoint() {
        }

        @RateLimited({"demo", "payments"})
        public void multipleRulesEndpoint() {
        }
    }
}