package com.rateLimiter.distributedratelimiter.http;

import com.rateLimiter.distributedratelimiter.annotation.RateLimited;
import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterRegistry registry;
    private final RateLimitRuleProvider ruleProvider;
    private final RateLimiterMetrics metrics;

    public RateLimitInterceptor(
            RateLimiterRegistry registry,
            RateLimitRuleProvider ruleProvider,
            RateLimiterMetrics metrics) {

        this.registry = Objects.requireNonNull(
                registry,
                "Registry must not be null");

        this.ruleProvider = Objects.requireNonNull(
                ruleProvider,
                "Rule provider must not be null");

        this.metrics = Objects.requireNonNull(
                metrics,
                "Metrics must not be null");
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {

        /*
         * Ignore non-controller handlers.
         */
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        /*
         * Check whether endpoint is rate limited.
         */
        RateLimited annotation =
                handlerMethod.getMethodAnnotation(
                        RateLimited.class);

        /*
         * No annotation present.
         */
        if (annotation == null) {
            return true;
        }

        String[] ruleNames = annotation.value();

        for (String ruleName : ruleNames) {

            RateLimitRule rule =
                    ruleProvider.getRule(ruleName);

            RateLimiter limiter =
                    registry.getLimiter(
                            rule.algorithm());

            /*
             * Endpoint-specific key.
             */
            String key =
                    request.getRemoteAddr()
                            + ":"
                            + request.getRequestURI();

            RateLimitResult result =
                    limiter.tryAcquire(key, rule);

            /*
             * Expose rate limit headers.
             */
            response.setHeader(
                    "X-RateLimit-Limit",
                    String.valueOf(rule.limit()));

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(result.remaining()));

            response.setHeader(
                    "X-RateLimit-Algorithm",
                    rule.algorithm().name());

            /*
             * Request blocked.
             */
            if (!result.allowed()) {

                metrics.recordBlocked(
                        rule.algorithm());

                response.setStatus(
                        HttpStatus.TOO_MANY_REQUESTS.value());

                response.setHeader(
                        "Retry-After",
                        String.valueOf(
                                Math.max(
                                        1,
                                        result.retryAfterMs() / 1000)));

                response.setContentType(
                        "application/json");

                response.getWriter().write(
                        """
                        {
                          "error":"rate limit exceeded"
                        }
                        """);

                return false;
            }

            metrics.recordAllowed(
                    rule.algorithm());
        }

        return true;
    }
}