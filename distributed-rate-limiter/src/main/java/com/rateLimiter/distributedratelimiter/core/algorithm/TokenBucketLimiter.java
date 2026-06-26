package com.rateLimiter.distributedratelimiter.core.algorithm;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketLimiter implements RateLimiter {

    private record Bucket(double availableTokens, long lastRefillNanos){}

    private final ClockProvider clockProvider;

    private static final double TOKEN_COST = 1.0;

    private final ConcurrentHashMap<String,Bucket> buckets=new ConcurrentHashMap<>();
    /**
     * Per-key locks ensure requests for different keys
     * do not block each other.
     * Lock cleanup is intentionally omitted in the
     * in-memory implementation because this algorithm
     * serves as a correctness prototype before moving
     * to Redis-backed storage.
     */
    private final ConcurrentHashMap<String, ReentrantLock> locks=new ConcurrentHashMap<>();

    public TokenBucketLimiter(ClockProvider clockProvider){
        this.clockProvider= Objects.requireNonNull(clockProvider,"ClockProvider must not be null");
    }

    @Override
    public RateLimitResult tryAcquire(String key,RateLimitRule rule){
        validateInputs(key,rule);
        ReentrantLock lock=locks.computeIfAbsent(key,ignored->new ReentrantLock());
        lock.lock();
        try {
            long now=clockProvider.nanoTime();
            Bucket bucket=buckets.get(key);
            double refillRatePerNano =(double) rule.limit()/rule.window().toNanos();
            double availableTokens;
            if(bucket==null){
                availableTokens=rule.limit();
            }else{
                long elapsedNanos=now-bucket.lastRefillNanos();
                double refilledTokens=elapsedNanos*refillRatePerNano;
                availableTokens=Math.min(rule.limit(), bucket.availableTokens()+refilledTokens);
            }
            if (availableTokens >= TOKEN_COST){
                double remainingTokens = availableTokens - TOKEN_COST;
                buckets.put(key,new Bucket(remainingTokens,now));
                return new RateLimitResult(true,(long) remainingTokens,0);
            }
            long nanosUntilNextToken=(long)((1.0-availableTokens)/refillRatePerNano);
            buckets.put(key,new Bucket(availableTokens,now));
            long retryAfterMillis=nanosUntilNextToken/1_000_000L;
            return new RateLimitResult(false,0,Math.max(retryAfterMillis,1));
        }finally {
            lock.unlock();
        }
    }

    private void validateInputs(String key,RateLimitRule rule){
        Objects.requireNonNull(key,"Key must be non null");
        Objects.requireNonNull(rule,"Rule must be non null");
        if(key.isBlank()) throw new IllegalArgumentException("Key must not be blank");
        if(rule.limit()<=0) throw new IllegalArgumentException("Limit must be greater than 0");
        if(rule.window().isZero() || rule.window().isNegative()) throw new IllegalArgumentException("Window must be greater than 0");
    }

}
