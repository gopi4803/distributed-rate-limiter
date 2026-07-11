package com.rateLimiter.distributedratelimiter.config;

import com.rateLimiter.distributedratelimiter.key.KeyExtractor;
import com.rateLimiter.distributedratelimiter.policy.RateLimitPolicy;
import com.rateLimiter.distributedratelimiter.policy.RateLimitRuleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("benchmark")
public class BenchmarkRateLimitPolicyConfiguration {

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
                        ruleProvider.getRule("benchmark")),

                new RateLimitPolicy(
                        userExtractor,
                        ruleProvider.getRule("payments"))
        );
    }
}