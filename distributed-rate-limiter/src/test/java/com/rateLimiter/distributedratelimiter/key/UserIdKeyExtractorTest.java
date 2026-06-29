package com.rateLimiter.distributedratelimiter.key;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class UserIdKeyExtractorTest {

    private final KeyExtractor extractor =
            new UserIdKeyExtractor();

    @Test
    void shouldExtractUserId() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-User-Id",
                "user-123");

        String key = extractor.extract(request);

        assertEquals("user:user-123", key);
    }

    @Test
    void shouldReturnNullWhenHeaderMissing() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        String key = extractor.extract(request);

        assertNull(key);
    }

    @Test
    void shouldReturnNullWhenHeaderBlank() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-User-Id",
                "   ");

        String key = extractor.extract(request);

        assertNull(key);
    }

    @Test
    void shouldReturnUserPrefix() {

        assertEquals("user", extractor.prefix());
    }
}