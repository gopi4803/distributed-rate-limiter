package com.rateLimiter.distributedratelimiter.core.clock;

import org.springframework.stereotype.Component;

@Component
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
