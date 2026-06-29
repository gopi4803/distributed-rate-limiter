# Distributed Rate Limiter

A production-oriented distributed rate limiter built from scratch using **Java**, **Spring Boot**, **Redis**, and **Lua scripting**.

The project explores how modern backend systems implement scalable, resilient, and distributed request throttling while maintaining correctness under concurrent load.

---

## Motivation

Modern distributed applications must protect themselves from:

* Traffic spikes
* Malicious or abusive clients
* Resource exhaustion
* Cascading failures

Implementing rate limiting in a distributed environment introduces several non-trivial challenges:

* Shared state management
* Race conditions
* Atomicity guarantees
* Horizontal scalability
* Fault tolerance
* Distributed consistency

This project demonstrates how these challenges can be addressed using production-inspired architectural patterns and distributed systems techniques.

---

## Features

### Core Features

* Pluggable rate limiting architecture
* Immutable domain model using Java Records
* Registry-based algorithm selection
* Deterministic testing through clock abstraction
* Domain-specific exception hierarchy

### Supported Algorithms

* Token Bucket
* Sliding Window Counter

### Distributed Features

* Redis-backed distributed state
* Redis Lua scripts for atomic execution
* Multi-instance distributed validation
* Redis server time as the single source of truth

### Resilience Features

* Fail-Open strategy
* Fail-Closed strategy
* Circuit Breaker support
* Graceful handling of Redis failures

### Engineering Features

* Thread-safe implementations
* Extensive unit and integration tests
* Deterministic concurrency testing
* Production-inspired package organization
* Extensible architecture for future algorithms

---

## High-Level Architecture

```text
                           Client Request
                                   |
                                   v
                     +-------------------------+
                     | RateLimiterRegistry     |
                     +-------------------------+
                                   |
                                   v
                +-----------------------------------+
                | Token Bucket / Sliding Window    |
                +-----------------------------------+
                                   |
                                   v
                +-----------------------------------+
                | CircuitBreakerRateLimiter        |
                +-----------------------------------+
                                   |
                                   v
                +-----------------------------------+
                | ResilientRateLimiter             |
                +-----------------------------------+
                                   |
                                   v
                       +---------------------+
                       | Redis + Lua Scripts |
                       +---------------------+
```

---

## Supported Algorithms

### Token Bucket

Maintains a bucket of tokens replenished continuously over time.

**Characteristics:**

* Allows short bursts of traffic
* Smooths request rate over time
* O(1) memory per key
* Widely used in production API gateways

### Sliding Window Counter

Combines counts from adjacent windows using weighted calculations.

**Characteristics:**

* Provides smoother rate limiting
* Reduces fixed-window boundary bursts
* O(1) memory per key
* Better request distribution across windows

---

## Distributed Design

The limiter stores rate limiting state inside Redis to ensure consistent enforcement across multiple application instances.

Redis Lua scripts are used to guarantee atomic execution of:

* Read current state
* Refill/update counters
* Consume tokens
* Persist updated state

Using Lua eliminates race conditions that occur with naive multi-command Redis implementations.

---

## Resilience Design

The project includes a dedicated resilience layer to handle infrastructure failures gracefully.

### Fail Open

Allows requests when Redis becomes unavailable.

Suitable for:

* Customer-facing APIs
* Authentication endpoints
* High-availability systems

### Fail Closed

Rejects requests when Redis becomes unavailable.

Suitable for:

* Expensive APIs
* Internal platform protection
* Strict quota enforcement

### Circuit Breaker

Protects Redis from repeated requests when the backing store becomes unhealthy.

Supported states:

* CLOSED
* OPEN
* HALF_OPEN

---

## Technology Stack

| Technology     | Purpose                                |
| -------------- | -------------------------------------- |
| Java 21        | Core implementation                    |
| Spring Boot    | Dependency injection and configuration |
| Redis          | Distributed shared state               |
| Lua            | Atomic operations                      |
| Maven          | Build management                       |
| JUnit 5        | Unit testing                           |
| Mockito        | Mocking framework                      |
| Testcontainers | Integration testing                    |
| Docker         | Containerization                       |

---

## Project Structure

```text
src/main/java
├── config
├── core
│   ├── algorithm
│   ├── clock
│   ├── model
│   ├── registry
│   └── utils
├── exceptions
├── metrics
├── redis
├── resilience
└── key
```

---

## Testing

The project contains:

* Unit tests for all algorithms
* Concurrency correctness tests
* Deterministic clock-based tests
* Integration tests using Testcontainers
* Redis failure handling tests
* Circuit breaker tests

Run all tests:

```bash
mvn clean test
```

Integration tests require Docker to be running locally.

---

## Current Progress

### Completed

* Core abstractions
* Token Bucket algorithm
* Sliding Window Counter algorithm
* Redis integration
* Redis Lua script support
* Distributed correctness validation
* Registry-based runtime selection
* Resilience layer
* Circuit Breaker implementation
* Exception hierarchy
* Comprehensive testing suite

---

## Roadmap

### Planned

* Fixed Window algorithm
* Annotation-based API (`@RateLimited`)
* Spring AOP integration
* Servlet filter integration
* Dynamic policy management
* Micrometer metrics integration
* Prometheus support
* Benchmarking and load testing
* OpenTelemetry support

---

## Documentation

Detailed architecture and implementation documents are available in the `docs/` directory.

Examples include:

* Architecture overview
* Algorithm comparison
* Redis integration design
* Resilience design
* Failure mode analysis
* Benchmark results
* Testing strategy

---

## Status

Project is under active development and is being implemented incrementally in phases.

Each implementation phase is represented through meaningful Git commit history.
