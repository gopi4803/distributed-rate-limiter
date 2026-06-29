package com.rateLimiter.distributedratelimiter.http;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry registry;
    private final List<RateLimitPolicy> policies;
    private final RateLimiterMetrics metrics;

    public RateLimitFilter(
            RateLimiterRegistry registry,
            List<RateLimitPolicy> policies,
            RateLimiterMetrics metrics) {

        this.registry = Objects.requireNonNull(registry);
        this.metrics = Objects.requireNonNull(metrics);
        this.policies = List.copyOf(
                Objects.requireNonNull(policies));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        for (RateLimitPolicy policy : policies) {

            String extractedKey =
                    policy.extractor().extract(request);

            if (extractedKey == null) {
                continue;
            }

            String key =
                    extractedKey + ":" + request.getRequestURI();

            RateLimitRule rule = policy.rule();

            RateLimiter limiter =
                    registry.getLimiter(rule.algorithm());

            RateLimitResult result =
                    limiter.tryAcquire(key, rule);

            response.setHeader(
                    "X-RateLimit-Limit",
                    String.valueOf(rule.limit()));

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(result.remaining()));

            response.setHeader(
                    "X-RateLimit-Algorithm",
                    rule.algorithm().name());

            if (!result.allowed()) {

                metrics.recordBlocked(
                        rule.algorithm());

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(
                        "Retry-After",
                        String.valueOf(
                                Math.max(
                                        1,
                                        result.retryAfterMs() / 1000)));

                response.setHeader(
                        "X-RateLimit-LimitedBy",
                        policy.extractor().prefix());

                response.setContentType(
                        "application/json");

                response.getWriter().write(
                        """
                        {
                          "error":"rate limit exceeded"
                        }
                        """);

                return;
            }
            metrics.recordAllowed(rule.algorithm());
        }



        /*
         * All policies passed.
         */

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.startsWith("/actuator");
    }
}