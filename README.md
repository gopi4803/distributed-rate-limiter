# Distributed Rate Limiter

A production-oriented distributed rate limiter built from scratch using **Java**, **Spring Boot**, **Redis**, **Lua scripting**, and **Docker**.

The project explores how modern backend systems implement scalable, resilient, and distributed request throttling while maintaining correctness under concurrent load.

---

# Motivation

Modern distributed systems must protect themselves from:

- Traffic spikes
- Malicious or abusive clients
- Resource exhaustion
- Cascading failures

Implementing rate limiting in a distributed environment introduces several non-trivial challenges:

- Shared distributed state
- Race conditions
- Atomicity guarantees
- Horizontal scalability
- Failure handling
- Distributed consistency
- Concurrency correctness

This project demonstrates how these challenges can be solved using production-inspired architectural patterns and distributed systems techniques.

---

# Features

## Core Features

- Pluggable rate limiting architecture
- Immutable domain model using Java Records
- Registry-based runtime algorithm selection
- Deterministic testing using clock abstraction
- Dynamic rule configuration
- Extensible architecture for future algorithms

---

## Supported Algorithms

- Token Bucket
- Sliding Window Counter
- Fixed Window

---

## Distributed Features

- Redis-backed distributed state management
- Redis Lua scripting for atomic execution
- Redis server time as the single source of truth
- Multi-instance distributed deployment
- Global quota enforcement across application instances
- Elimination of race conditions through server-side Lua scripting

---

## Resilience Features

- Fail Open strategy
- Fail Closed strategy
- Circuit Breaker support
- Graceful degradation during Redis failures

---

## HTTP Integration Features

- Global request interception using `OncePerRequestFilter`
- Multiple simultaneous rate limiting policies
- Pluggable key extraction
- HTTP 429 responses
- Retry-After support
- Standard Rate Limit headers

---

## Observability Features

- Micrometer metrics
- Spring Boot Actuator
- Prometheus metrics
- Per-algorithm metrics tagging
- Request duration metrics

---

## Infrastructure Features

- Dockerized Spring Boot application
- Multi-stage Docker build
- Docker Compose deployment
- Three-node distributed cluster
- Shared Redis deployment

---

# High-Level Architecture

```text
                          Client Requests
                                 |
                                 v
                    +---------------------------+
                    |      Load Balancer        |
                    +---------------------------+
                      /            |            \
                     /             |             \
                    v              v              v

             +------------+ +------------+ +------------+
             | App Node 1 | | App Node 2 | | App Node 3 |
             | Port 8080  | | Port 8081  | | Port 8082  |
             +------------+ +------------+ +------------+
                    \            |            /
                     \           |           /
                      \          |          /
                               v
                    +---------------------------+
                    |        Redis + Lua        |
                    +---------------------------+
```

---

# HTTP Request Flow

```text
Client Request
      |
      v
OncePerRequestFilter
      |
      v
Key Extractor
      |
      v
Rate Limit Policy
      |
      v
RateLimiterRegistry
      |
      v
Selected Algorithm
      |
      v
Circuit Breaker
      |
      v
Resilience Layer
      |
      v
Redis Lua Script
```

---

# Supported Algorithms

## Token Bucket

- Allows short bursts
- Smooth request rate
- O(1) memory per key
- Production API Gateway friendly

---

## Sliding Window Counter

- Smooth rate limiting
- Reduces burst effects
- Weighted approximation
- O(1) memory

---

## Fixed Window

- Simple implementation
- O(1) memory
- Fast execution

---

# Distributed Design

Rate limiting state is stored inside Redis.

Each request executes an atomic Lua script that:

- Reads current state
- Calculates refill or window values
- Applies the request
- Persists updated state

This guarantees:

- Atomic execution
- No race conditions
- Single Redis round trip
- Correct distributed behavior

---

# Multi-Instance Deployment

The application has been validated using Docker Compose with three independent Spring Boot instances sharing a single Redis instance.

```text
App1 (8080)
       \
App2 (8081) ---> Shared Redis
       /
App3 (8082)
```

Validation proved:

- Shared distributed quota
- Cross-instance consistency
- Global rate limit enforcement

Example:

```
App1 -> Remaining 4
App2 -> Remaining 3
App3 -> Remaining 2
App1 -> Remaining 1
App2 -> Remaining 0
App3 -> HTTP 429
```

This demonstrates that all application instances enforce one global quota.

---

# Key Abstraction

The limiter core remains completely independent of HTTP concerns.

Core API:

```java
tryAcquire(String key, RateLimitRule rule)
```

Supported extractors:

- IP Address
- User ID

Additional extractors can be introduced without modifying the limiter implementation.

---

# Resilience

Supported failure strategies:

## Fail Open

Requests continue when Redis becomes unavailable.

Suitable for:

- Customer APIs
- Authentication
- High availability

---

## Fail Closed

Requests are rejected when Redis becomes unavailable.

Suitable for:

- Internal services
- Strict quota enforcement

---

## Circuit Breaker

Supported states:

- CLOSED
- OPEN
- HALF_OPEN

Features include:

- Configurable thresholds
- Automatic recovery
- Single probe request

---

# Metrics

Metrics exposed through:

```
/actuator/metrics
/actuator/prometheus
/actuator/health
```

Example metrics:

```
ratelimiter.requests.allowed
ratelimiter.requests.blocked
ratelimiter.redis.failures
ratelimiter.circuitbreaker.open.transitions
ratelimiter.request.duration
```

---

# Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Core implementation |
| Spring Boot | Application framework |
| Redis | Distributed shared state |
| Lua | Atomic execution |
| Docker | Containerization |
| Docker Compose | Multi-instance deployment |
| Maven | Build |
| Micrometer | Metrics |
| Actuator | Observability |
| Prometheus | Metrics export |
| JUnit 5 | Testing |
| Mockito | Mocking |
| Testcontainers | Integration testing |

---

# Testing

The project contains:

- Unit tests
- Integration tests
- Redis integration tests
- Circuit breaker tests
- Resilience tests
- HTTP filter tests
- Registry tests
- Configuration tests
- Docker-based distributed validation
- Multi-instance correctness validation

Run:

```bash
mvn clean test
```

---

# Current Progress

## Completed

- Core abstractions
- Token Bucket
- Sliding Window Counter
- Fixed Window
- Redis integration
- Redis Lua scripting
- Registry architecture
- Dynamic rule configuration
- HTTP filter integration
- Key extraction layer
- Circuit Breaker
- Fail Open / Fail Closed
- Micrometer metrics
- Prometheus integration
- Docker support
- Docker Compose deployment
- Multi-instance distributed validation
- Comprehensive testing suite

---

# Roadmap

## Next Phase

### Performance Benchmarking

- k6 load testing
- Throughput benchmarking
- Latency benchmarking
- Algorithm comparison
- Stress testing
- Benchmark reports

---

## Future Enhancements

- Dynamic rule reloading
- API Key extractor
- Tenant-based rate limiting
- OpenTelemetry tracing
- Redis Cluster support

---

# Documentation

The `docs/` directory contains detailed documentation covering:

- System architecture
- Algorithm comparison
- Redis integration
- Lua scripting
- Distributed deployment
- Resilience design
- Metrics
- Testing strategy
- Performance benchmarking (planned)

---

# Status

 **Feature Complete**

Completed phases:

```
✓ Foundation
✓ Core Algorithms
✓ Redis Integration
✓ Lua Scripting
✓ Resilience Layer
✓ Metrics & Observability
✓ Dynamic Configuration
✓ HTTP Filter Integration
✓ Docker Support
✓ Multi-Instance Distributed Deployment
```

Current focus:

```
Performance Benchmarking
Stress Testing
Documentation
Project Polish
```

The project has successfully demonstrated distributed rate limiting across multiple Spring Boot instances sharing a common Redis backend with globally enforced quotas.
