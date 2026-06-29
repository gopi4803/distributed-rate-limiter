package com.rateLimiter.distributedratelimiter.key;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpKeyExtractorTest {

    private final KeyExtractor extractor = new IpKeyExtractor();

    @Test
    void shouldExtractForwardedIp() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-Forwarded-For",
                "10.1.1.1");

        String key = extractor.extract(request);

        assertEquals("ip:10.1.1.1", key);
    }

    @Test
    void shouldExtractFirstIpWhenMultipleForwardedIpsPresent() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-Forwarded-For",
                "10.1.1.1, 10.1.1.2, 10.1.1.3");

        String key = extractor.extract(request);

        assertEquals("ip:10.1.1.1", key);
    }

    @Test
    void shouldFallbackToRemoteAddressWhenForwardedHeaderMissing() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("192.168.1.100");

        String key = extractor.extract(request);

        assertEquals("ip:192.168.1.100", key);
    }

    @Test
    void shouldFallbackToRemoteAddressWhenForwardedHeaderBlank() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-Forwarded-For",
                "   ");

        request.setRemoteAddr("192.168.1.100");

        String key = extractor.extract(request);

        assertEquals("ip:192.168.1.100", key);
    }

    @Test
    void shouldReturnIpPrefix() {

        assertEquals("ip", extractor.prefix());
    }
}