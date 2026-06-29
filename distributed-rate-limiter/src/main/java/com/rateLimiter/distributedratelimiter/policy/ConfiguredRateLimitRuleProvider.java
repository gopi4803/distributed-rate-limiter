package com.rateLimiter.distributedratelimiter.policy;

import com.rateLimiter.distributedratelimiter.config.RateLimiterProperties;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.exceptions.RateLimiterException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConfiguredRateLimitRuleProvider implements RateLimitRuleProvider{

    private final RateLimiterProperties properties;
    private Map<String, RateLimitRule> rules;

    @PostConstruct
    void initialize(){
        if(properties.getRules().isEmpty()){
            throw new RateLimiterException("No rate limit rules configured");
        }
        this.rules=properties.getRules()
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> {
                            String ruleName=entry.getKey();
                            var config=entry.getValue();
                            return new RateLimitRule(ruleName, config.getLimit(), config.getWindow(),config.getAlgorithm());
                        }
                ));
    }

    @Override
    public RateLimitRule getRule(String ruleName){
        RateLimitRule rule=rules.get(ruleName);
        if(rule==null){
            throw new RateLimiterException("Unknown Rate limit rule: "+ruleName);
        }
        return rule;
    }

}
