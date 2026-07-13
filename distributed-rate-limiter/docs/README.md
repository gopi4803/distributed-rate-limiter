# Documentation

The root `README.md` provides a high-level overview of the project, while this directory contains detailed technical documentation covering the system architecture, benchmark framework, implementation details, and design decisions.

Documentation is organized by topic so individual components can evolve independently without overloading the root README.

---

# Documentation Index

| Documentation | Description |
|--------------|-------------|
| [`architecture/`](architecture/README.md) | System architecture, request flow, deployment architecture, and component interactions. |
| [`algorithms/`](algorithms/README.md) | Detailed implementation and analysis of the supported rate limiting algorithms. |
| [`redis/`](redis/README.md) | Distributed state management, Redis data structures, Lua scripting, and atomic operations. |
| [`benchmark/`](benchmark/README.md) | Benchmark methodology, execution framework, infrastructure metrics, behavioural analysis, and performance reports. |
| [`testing/`](testing/README.md) | Unit, integration, concurrency, and Testcontainers-based testing strategy. |
| [`resilience/`](resilience/README.md) | Circuit Breaker implementation, Fail Open / Fail Closed behaviour, and resilience design. |
---

# Documentation Structure

```text
docs/
│
├── README.md
│
├── architecture/
│   ├── system-overview.md
│   ├── request-flow.md
│   └── deployment.md
│
├── benchmark/
│   ├── methodology.md
│   ├── environment.md
│   ├── infrastructure.md
│   ├── behavioural.md
│   ├── metrics.md
│   └── analysis.md
│
├── redis/
│   ├── lua-scripting.md
│   ├── distributed-state.md
│   └── atomic-operations.md
│
├── algorithms/
│   ├── token-bucket.md
│   ├── sliding-window.md
│   └── fixed-window.md
│
├── testing/
│   ├── unit-tests.md
│   ├── integration-tests.md
│   └── concurrency-tests.md
│
└── resilience/
    ├── circuit-breaker.md
    └── fail-open.md
```

The directory structure is intentionally modular so that each topic can be documented independently.

---

# Reading Guide

If you're exploring the project for the first time, the following reading order is recommended.

## 1. Project Overview

Start with the repository root:

```text
README.md
```

This provides:

* Project motivation
* Feature overview
* Architecture
* Deployment modes
* Benchmark highlights

---

## 2. Benchmark Framework

Continue with:

```text
benchmark/README.md
```

This explains:

* Benchmark architecture
* Automation framework
* Benchmark scenarios
* Metrics collection
* Generated artifacts

---

## 3. Detailed Technical Documentation

Explore the documentation under `docs/` based on your area of interest.

For example:

**Architecture**

Understand how requests flow through the system and how components interact.

**Algorithms**

Learn how each rate limiting algorithm works and the trade-offs involved.

**Redis**

Explore distributed state management, Lua scripting, and atomic execution.

**Benchmarking**

Understand the benchmark methodology, environment, metrics, and performance analysis.

**Testing**

Review the testing strategy, including unit, integration, concurrency, and Testcontainers-based testing.

---

# Documentation Principles

The documentation follows a few simple principles.

* Each document focuses on a single topic.
* High-level concepts remain in the root README.
* Detailed implementation belongs under `docs/`.
* Benchmark execution is documented separately under `benchmark/README.md`.
* Documentation evolves alongside the implementation.

This organization keeps individual documents concise while making the overall project easier to navigate.

---

## Contributing

When introducing new features or architectural changes, please update the relevant documentation alongside the implementation.