# Distributed Rate Limiter

A production-inspired distributed rate limiter built from scratch using Java, Spring Boot, Redis, and Lua scripting.

The primary goal of this project is to explore distributed systems concepts such as:

* Rate limiting algorithms
* Shared state management
* Concurrency and race conditions
* Atomic operations
* Horizontal scalability
* Fault tolerance
* Observability and benchmarking

## Motivation

Modern distributed applications must protect themselves from abuse, traffic spikes, and resource exhaustion.

This project demonstrates how production systems implement request throttling across multiple application instances while maintaining correctness under concurrent load.

## Planned Features

* Token Bucket algorithm
* Sliding Window Counter algorithm
* Redis-backed distributed state
* Lua-based atomic operations
* Pluggable key extraction (IP/User ID)
* Multiple rate limiting policies
* Servlet filter-based request interception
* Circuit breaker support for Redis failures
* Metrics and observability
* Load testing and benchmarking
* Multi-instance distributed validation

## Tech Stack

* Java 21
* Spring Boot
* Redis
* Lua
* Docker
* JUnit 5
* Testcontainers
* Resilience4j
* Micrometer
* k6

## Project Status
 
Under active development.

Implementation is being developed incrementally in phases with each phase represented through Git commit history.

## High-Level Architecture

```text
Client
   |
Load Balancer
   |
-------------------------
|                       |
App Instance 1    App Instance 2
|                       |
-------------------------
           |
         Redis
```
## Current Progress

### Phase 0 - Core Abstractions 

Implemented:

* `RateLimiter` contract
* `RateLimitRule`
* `RateLimitResult`
* Clock abstraction (`ClockProvider`)
* Algorithm registry

### Phase 1 - In-Memory Algorithms 

Implemented:

* Token Bucket algorithm
* Sliding Window Counter algorithm

Features:

* Thread-safe implementation
* Per-key concurrency control
* Deterministic time abstraction for testing

### Phase 2 - Redis Integration (Naive Implementation) 

Objective:

Move rate limiter state from JVM memory to Redis to support distributed deployments.

Implemented:

* Redis integration using Spring Data Redis (`StringRedisTemplate`)
* Dockerized Redis for local development
* Redis-backed distributed rate limiter
* Redis key generation utility
* Integration testing using Testcontainers

Redis key format:

```text
ratelimit:bucket:<key>
```

Example:

```text
ratelimit:bucket:user-123
```

### Distributed Race Condition Demonstration

The initial Redis implementation intentionally performs:

```text
GET -> CHECK LIMIT -> INCREMENT
```

These operations are executed as separate Redis commands and are therefore not atomic.

A dedicated concurrency integration test was implemented using:

* 500 concurrent threads
* Shared Redis key
* Configured limit: 10 requests

Observed result:

```text
Allowed Requests = 500
```

This demonstrates that naive read-check-update logic is unsafe in distributed systems and may significantly over-allocate requests under concurrent load.

### Next Phase

Replace the naive Redis implementation with Redis Lua scripts to guarantee atomic execution and eliminate race conditions.
