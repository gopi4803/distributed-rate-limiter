package com.rateLimiter.distributedratelimiter.core.clock;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableClockProviderTest {

    @Test
    void shouldStartWithProvidedTime(){
        MutableClockProvider clock=new MutableClockProvider(1000);
        assertEquals(1000,clock.currentTimeMillis());
    }

    @Test
    void shouldAdvanceTimeBySpecificDuration(){
        MutableClockProvider clock=new MutableClockProvider(0);
        clock.advance(Duration.ofSeconds(10));
        assertEquals(10_000,clock.currentTimeMillis());
    }

    @Test
    void shouldAdvanceMultipleTimes(){
        MutableClockProvider clock=new MutableClockProvider(0);
        clock.advance(Duration.ofSeconds(30));
        clock.advance(Duration.ofSeconds(50));
        assertEquals(80_000,clock.currentTimeMillis());
    }

    @Test
    void shouldReturnNanoTime(){
        MutableClockProvider clock=new MutableClockProvider(1);
        assertEquals(1_000_000L,clock.nanoTime());
    }

    @Test
    void shouldSetCurrentTime(){
        MutableClockProvider clock=new MutableClockProvider();
        clock.setCurrentTimeMillis(5000);
        assertEquals(5000,clock.currentTimeMillis());
        assertEquals(5_000_000_000L,clock.nanoTime());
    }
}
