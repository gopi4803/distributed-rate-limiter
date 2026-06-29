package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.core.registry.RateLimiterRegistry;
import com.rateLimiter.distributedratelimiter.http.RateLimitFilter;
import com.rateLimiter.distributedratelimiter.key.IpKeyExtractor;
import com.rateLimiter.distributedratelimiter.key.KeyExtractor;
import com.rateLimiter.distributedratelimiter.key.UserIdKeyExtractor;
import com.rateLimiter.distributedratelimiter.metrics.RateLimiterMetrics;
import com.rateLimiter.distributedratelimiter.policy.RateLimitPolicy;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RateLimitFilterConfiguration {

    @Bean
    public KeyExtractor ipKeyExtractor() {
        return new IpKeyExtractor();
    }

    @Bean
    public KeyExtractor userIdKeyExtractor() {
        return new UserIdKeyExtractor();
    }

    @Bean
    public List<RateLimitPolicy> rateLimitPolicies(
            @Qualifier("ipKeyExtractor")
            KeyExtractor ipExtractor,

            @Qualifier("userIdKeyExtractor")
            KeyExtractor userExtractor,

            RateLimitRuleProvider ruleProvider) {

        return List.of(
                new RateLimitPolicy(
                        ipExtractor,
                        ruleProvider.getRule("demo")),

                new RateLimitPolicy(
                        userExtractor,
                        ruleProvider.getRule("payments"))
        );
    }

    @Bean
    public RateLimitFilter rateLimitFilter(
            RateLimiterRegistry registry,
            List<RateLimitPolicy> policies,
            RateLimiterMetrics metrics) {

        return new RateLimitFilter(
                registry,
                policies,
                metrics);
    }

//    @Bean
//    public FilterRegistrationBean<RateLimitFilter>
//    filterRegistrationBean(
//            RateLimitFilter filter) {
//
//        FilterRegistrationBean<RateLimitFilter> registration =
//                new FilterRegistrationBean<>();
//
//        registration.setFilter(filter);
//
//        registration.addUrlPatterns("/*");
//
//        registration.setOrder(1);
//
//        return registration;
//    }
}
