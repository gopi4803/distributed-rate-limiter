package com.rateLimiter.distributedratelimiter.core.algorithm;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.utils.ValidationUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketLimiter implements RateLimiter {

    private record Bucket(double availableTokens, long lastRefillNanos){}

    private final ClockProvider clockProvider;

    /**
     * TOKEN_COST is currently fixed at 1 token per request.
     * Retry-after calculation assumes a constant request cost.
     * Supporting weighted requests would require replacing
     * TOKEN_COST with a per-request cost.
     */
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
        ValidationUtils.validateInputs(key,rule);
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
                System.out.println(
                        "elapsedNanos=" + elapsedNanos
                                + ", refilled=" + refilledTokens
                                + ", availableAfterRefill=" + availableTokens);
            }
            System.out.println(
                    "KEY=" + key +
                            ", NOW=" + now +
                            ", AVAILABLE_BEFORE=" + availableTokens +
                            ", LIMIT=" + rule.limit());
            if (availableTokens >= TOKEN_COST){
                double remainingTokens = availableTokens - TOKEN_COST;
                System.out.println(
                        "ALLOWED -> remaining=" + remainingTokens);
                buckets.put(key,new Bucket(remainingTokens,now));
                return new RateLimitResult(true,(long) remainingTokens,0);
            }
            long nanosUntilNextToken=(long)((TOKEN_COST-availableTokens)/refillRatePerNano);
            buckets.put(key,new Bucket(availableTokens,now));
            long retryAfterMillis=nanosUntilNextToken/1_000_000L;
            System.out.println(
                    "BLOCKED -> retryAfterMillis=" + retryAfterMillis);
            return new RateLimitResult(false,0,Math.max(retryAfterMillis,1));
        }finally {
            lock.unlock();
        }
    }

}
