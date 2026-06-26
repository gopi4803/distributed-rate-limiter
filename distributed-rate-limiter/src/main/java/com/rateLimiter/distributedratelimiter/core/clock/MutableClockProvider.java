package com.rateLimiter.distributedratelimiter.core.clock;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class MutableClockProvider implements ClockProvider{
    private final AtomicLong millis;
    private final AtomicLong nanos;

    public MutableClockProvider(){
        this(0);
    }

    public MutableClockProvider(long startMillis){
        this.millis=new AtomicLong(startMillis);
        this.nanos=new AtomicLong(startMillis*1_000_000L);
    }

    public void advance(Duration duration){
        millis.addAndGet(duration.toMillis());
        nanos.addAndGet(duration.toNanos());
    }

    public void setCurrentTimeMillis(long currentTimeMillis){
        millis.set(currentTimeMillis);
        nanos.set(currentTimeMillis*1_000_000L);
    }

    @Override
    public long currentTimeMillis(){
        return millis.get();
    }

    @Override
    public long nanoTime(){
        return nanos.get();
    }
}
