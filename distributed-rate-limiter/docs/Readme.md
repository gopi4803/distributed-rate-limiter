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
