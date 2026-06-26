package com.rateLimiter.distributedratelimiter.core.clock;

public class SystemClockProvider implements ClockProvider{

    @Override
    public long currentTimeMillis(){
        return System.currentTimeMillis();
    }

    @Override
    public long nanoTime(){
        return System.nanoTime();
    }
}
