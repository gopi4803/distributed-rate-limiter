# Distributed Rate Limiter

A production-oriented distributed rate limiter built from scratch using **Java**, **Spring Boot**, **Redis**, and **Lua scripting**.

The project explores how modern backend systems implement scalable, resilient, and fault-tolerant request throttling while maintaining correctness under concurrent load.

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
- Comprehensive validation and exception handling
- Extensible architecture for future algorithms

## Supported Algorithms

- Token Bucket
- Sliding Window Counter
- Fixed Window

## Distributed Features

- Redis-backed distributed state management
- Redis Lua scripting for atomic execution
- Redis server time used as the single source of truth
- Elimination of race conditions through server-side scripting

## Resilience Features

- Fail-Open strategy
- Fail-Closed strategy
- Circuit Breaker support
- Graceful degradation during Redis failures

## HTTP Integration Features

- Global request interception using `OncePerRequestFilter`
- Endpoint-specific rate limiting
- Multiple simultaneous rate limit policies
- Pluggable key extraction layer
- HTTP 429 responses
- Retry-After header support
- Standard rate limit response headers

## Observability Features

- Micrometer metrics integration
- Spring Boot Actuator support
- Prometheus-compatible metrics
- Per-algorithm metrics tagging
- Request duration tracking

## Engineering Features

- Thread-safe implementations
- Extensive unit and integration tests
- Deterministic concurrency testing
- Redis integration testing using Testcontainers
- Production-inspired package organization

---

# High-Level Architecture

```text
                           Client Request
                                   |
                                   v
                   +--------------------------------+
                   | RateLimitFilter               |
                   | (OncePerRequestFilter)        |
                   +--------------------------------+
                                   |
                                   v
                   +--------------------------------+
                   | Key Extractors                |
                   | - IP                          |
                   | - User ID                    |
                   +--------------------------------+
                                   |
                                   v
                   +--------------------------------+
                   | RateLimitPolicy              |
                   +--------------------------------+
                                   |
                                   v
                   +--------------------------------+
                   | RateLimiterRegistry          |
                   +--------------------------------+
                                   |
                                   v
             +------------------------------------------------+
             | Token Bucket / Sliding Window / Fixed Window   |
             +------------------------------------------------+
                                   |
                                   v
                   +--------------------------------+
                   | CircuitBreakerRateLimiter     |
                   +--------------------------------+
                                   |
                                   v
                   +--------------------------------+
                   | ResilientRateLimiter          |
                   +--------------------------------+
                                   |
                                   v
                      +---------------------------+
                      | Redis + Lua Scripts       |
                      +---------------------------+
```

---

# HTTP Architecture

The project initially implemented annotation-driven rate limiting using Spring MVC interceptors.

During development, the architecture evolved to a filter-based approach using `OncePerRequestFilter`.

## Why the change?

Spring MVC interceptors execute only for successfully mapped controller methods.

This can leave gaps for:

- Unmapped endpoints (404 probing)
- Malformed requests
- Requests blocked before handler resolution

The final architecture uses servlet filters because they guarantee interception of **every incoming request**, making the implementation closer to production API gateways and infrastructure components.

---

# Supported Algorithms

## Token Bucket

Maintains a bucket of tokens replenished continuously over time.

### Characteristics

- Allows short bursts of traffic
- Smooths request rates over time
- O(1) memory per key
- Widely used in production API gateways

---

## Sliding Window Counter

Combines counts from adjacent windows using weighted calculations.

### Characteristics

- Smoother request distribution
- Reduces fixed-window boundary bursts
- O(1) memory per key
- Weighted approximation of a true sliding window

---

## Fixed Window

Counts requests within discrete time windows.

### Characteristics

- Simple implementation
- O(1) memory per key
- Can suffer from boundary burst issues

---

# Distributed Design

Rate limiting state is stored in Redis to ensure consistent enforcement across distributed application instances.

Redis Lua scripts execute atomically on the Redis server and perform:

- Read current state
- Refill tokens or update counters
- Consume tokens
- Persist updated state

Using Lua guarantees:

- Single round-trip execution
- Atomicity
- Elimination of race conditions
- Consistent behavior under concurrency

---

# Key Abstraction Design

The limiter core is intentionally independent of request details.

The core API accepts:

```java
tryAcquire(String key, RateLimitRule rule)
```

The limiter itself has no knowledge of:

- IP addresses
- User identities
- API keys
- Tenants

These concerns are delegated to pluggable `KeyExtractor` implementations.

Currently supported:

- `IpKeyExtractor`
- `UserIdKeyExtractor`

This design allows additional dimensions to be added without modifying the limiter core.

Examples:

```text
ip:10.0.0.1:/payments
user:123:/payments
```

---

# Resilience Design

The resilience layer protects the application during infrastructure failures.

## Failure Strategies

### Fail Open

Requests are allowed when Redis becomes unavailable.

Suitable for:

- Customer-facing APIs
- Authentication systems
- High availability environments

### Fail Closed

Requests are rejected when Redis becomes unavailable.

Suitable for:

- Expensive APIs
- Internal platform protection
- Strict quota enforcement

---

## Circuit Breaker

Supported states:

- CLOSED
- OPEN
- HALF_OPEN

The implementation includes:

- Configurable failure thresholds
- Configurable recovery windows
- Single probe request during HALF_OPEN state
- Automatic recovery detection

---

# Metrics and Observability

Metrics are exposed through:

```text
/actuator/metrics
/actuator/prometheus
/actuator/health
```

Example metrics:

```text
ratelimiter.requests.allowed
ratelimiter.requests.blocked
ratelimiter.redis.failures
ratelimiter.circuitbreaker.open.transitions
ratelimiter.request.duration
```

Metrics are tagged by algorithm:

```text
TOKEN_BUCKET
SLIDING_WINDOW_COUNTER
FIXED_WINDOW
```

---

# Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Core implementation |
| Spring Boot | Dependency Injection and configuration |
| Redis | Distributed shared state |
| Lua | Atomic server-side execution |
| Maven | Build management |
| Micrometer | Metrics collection |
| Spring Boot Actuator | Observability |
| Prometheus | Metrics export |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |
| Testcontainers | Integration testing |
| Docker | Containerization |

---

# Project Structure

```text
src/main/java
├── config
├── controller
├── core
│   ├── algorithm
│   ├── clock
│   ├── model
│   └── registry
├── exceptions
├── http
├── key
├── metrics
├── policy
├── redis
└── resilience
```

---

# Testing

The project includes:

- Unit tests for all algorithms
- Redis integration tests
- Concurrency correctness tests
- Deterministic clock-based tests
- Circuit breaker tests
- Resilience tests
- HTTP integration tests
- Filter tests
- Registry tests
- Dynamic configuration tests

Run all tests:

```bash
mvn clean test
```

Integration tests require Docker to be running locally.

---

# Current Progress

## Completed

- Core abstractions
- Token Bucket algorithm
- Sliding Window Counter algorithm
- Fixed Window algorithm
- Redis integration
- Redis Lua scripting
- Registry-based algorithm resolution
- Dynamic configuration support
- Key extraction layer
- Global HTTP filtering
- Resilience layer
- Circuit Breaker implementation
- Metrics and observability
- Comprehensive testing suite

---

# Roadmap

## Next Phase

### Phase 7 — Multi-Instance Validation

- Multiple Spring Boot instances
- Shared Redis deployment
- Docker Compose setup
- Distributed correctness validation

## Future Enhancements

- Load testing using k6
- Benchmark generation
- OpenTelemetry integration
- Dynamic rule reloading
- API key extraction
- Tenant-based rate limiting

---

# Documentation

Additional documentation is available in the `docs/` directory.

Examples include:

- Architecture overview
- Algorithm comparison
- Redis integration design
- Failure mode analysis
- Benchmark results
- Testing strategy

---

# Status

🚧 **Project currently under active development**

Current completion status:

```text
Phase 0  ✓ Foundation
Phase 1  ✓ Algorithms
Phase 2  ✓ Distributed Redis
Phase 3  ✓ Resilience
Phase 4  ✓ Observability
Phase 5  ✓ Dynamic Configuration
Phase 6  ✓ Spring Integration
Phase 6.5 ✓ Filter-based HTTP Enforcement

Next:
Phase 7 → Multi-Instance Validation
```

Each implementation phase is represented through meaningful Git commit history.
