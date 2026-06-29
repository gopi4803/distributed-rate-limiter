package com.rateLimiter.distributedratelimiter.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedisProperties(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.redis.host",
                redisContainer::getHost);

        registry.add(
                "spring.data.redis.port",
                () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {

        connectionFactory.getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    void shouldAllowRequestsWithinLimit()
            throws Exception {

        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                            get("/api/v1/rate-limit/demo"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded()
            throws Exception {

        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                            get("/api/v1/rate-limit/demo"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(
                        get("/api/v1/rate-limit/demo"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldExposeRateLimitHeaders()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/rate-limit/demo"))
                .andExpect(status().isOk())
                .andExpect(
                        header().exists(
                                "X-RateLimit-Limit"))
                .andExpect(
                        header().exists(
                                "X-RateLimit-Remaining"))
                .andExpect(
                        header().exists(
                                "X-RateLimit-Algorithm"));
    }


    @Test
    void shouldBypassActuatorEndpoints()
            throws Exception {

        for (int i = 0; i < 50; i++) {

            mockMvc.perform(
                            get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}