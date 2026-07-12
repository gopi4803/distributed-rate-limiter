package com.rateLimiter.distributedratelimiter.core.algorithm;

import com.rateLimiter.distributedratelimiter.core.RateLimiter;
import com.rateLimiter.distributedratelimiter.core.clock.ClockProvider;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitResult;
import com.rateLimiter.distributedratelimiter.core.model.RateLimitRule;
import com.rateLimiter.distributedratelimiter.core.utils.ValidationUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounterLimiter implements RateLimiter {

    private record SlidingWindow(long currentWindowStartMillis, long currentWindowCount,long previousWindowCount){}

    private final ClockProvider clockProvider;

    private final ConcurrentHashMap<String,SlidingWindow> windows=new ConcurrentHashMap<>();
    /**
     * Per-key locks ensure requests for different keys
     * do not block each other.
     * Lock cleanup is intentionally omitted in the
     * in-memory implementation because this algorithm
     * serves as a correctness prototype before moving
     * to Redis-backed storage.
     */
    private final ConcurrentHashMap<String, ReentrantLock> locks=new ConcurrentHashMap<>();

    public SlidingWindowCounterLimiter(ClockProvider clockProvider){
        this.clockProvider= Objects.requireNonNull(clockProvider,"ClockProvider must be non null");
    }

    @Override
    public RateLimitResult tryAcquire(String key, RateLimitRule rule){
        ValidationUtils.validateInputs(key,rule);
        ReentrantLock lock=locks.computeIfAbsent(key,ignored->new ReentrantLock());
        lock.lock();
        try {
            long now=clockProvider.currentTimeMillis();
            long windowSizeMillis=rule.window().toMillis();
            long currentWindowStart=calculateWindowStart(now,windowSizeMillis);
            SlidingWindow window=windows.get(key);
            if(window==null){
                window=new SlidingWindow(currentWindowStart,0,0);
            }
            window=rollWindowIfNeeded(window,currentWindowStart,windowSizeMillis);
            double effectiveCount=calculateEffectiveCount(window,now,windowSizeMillis);
            if(effectiveCount<rule.limit()){
                SlidingWindow updatedWindow=new SlidingWindow(window.currentWindowStartMillis(),window.currentWindowCount()+1,window.previousWindowCount());
                windows.put(key,updatedWindow);
                return new RateLimitResult(true,rule.limit()-(long)effectiveCount-1,0);
            }
            long elapsedInCurrentWindow=now-window.currentWindowStartMillis();
            long retryAfterMillis=windowSizeMillis-elapsedInCurrentWindow;
            return new RateLimitResult(false,0,Math.max(retryAfterMillis,1));
        }finally {
            lock.unlock();
        }
    }

    private long calculateWindowStart(long nowMillis,long windowSizeMillis){
        return (nowMillis/windowSizeMillis)*windowSizeMillis;
    }

    private SlidingWindow rollWindowIfNeeded(SlidingWindow window,long currentWindowStart,long windowSizeMillis){
        if(window.currentWindowStartMillis()==currentWindowStart){
            return window;
        }
        long windowsPassed=(currentWindowStart-window.currentWindowStartMillis())/windowSizeMillis;
        if(windowsPassed==1){
            return new SlidingWindow(currentWindowStart,0,window.currentWindowCount());
        }
        return new SlidingWindow(currentWindowStart,0,0);
    }

    private double calculateEffectiveCount(SlidingWindow window,long nowMillis,long windowSizeMillis){
        long elapsedInCurrentWindow=nowMillis-window.currentWindowStartMillis();
        double elapsedFraction=(double) elapsedInCurrentWindow/windowSizeMillis;
        double previousWindowWeight=1.0-elapsedFraction;
        return (window.previousWindowCount()*previousWindowWeight+window.currentWindowCount());
    }

}
