# Benchmark Methodology

# Purpose

The purpose of benchmarking is to quantitatively evaluate the performance characteristics of the Distributed Rate Limiter under controlled and reproducible conditions.

The benchmarking phase validates not only the correctness of the implementation but also its scalability, efficiency, resilience, and operational behavior under varying workloads.

---

# Benchmark Objectives

The benchmarking process aims to answer the following engineering questions:

- How many requests per second can the system process?
- What latency does the rate limiter introduce?
- How does latency change under increasing load?
- Does throughput improve with additional application instances?
- Which rate limiting algorithm performs best?
- What component becomes the bottleneck first?
- How does the system behave during Redis failures?
- Are benchmark results reproducible?

---

# Benchmark Principles

All benchmarks performed in this project follow the following principles:

- Repeatability
- Reproducibility
- Fairness
- Isolation
- Incremental load increase
- Measurement over assumptions

No optimization decisions will be made without supporting benchmark data.

---

# Metrics Collected

Every benchmark records the following metrics.

## Throughput

- Requests per second (RPS)

---

## Latency

- Average latency
- Median latency (p50)
- 95th percentile latency (p95)
- 99th percentile latency (p99)
- Maximum latency

---

## Correctness

- Allowed requests
- Blocked requests
- Error responses
- HTTP status code distribution

---

## Resource Utilization

Application:

- CPU utilization
- Memory utilization

Redis:

- CPU utilization
- Memory utilization

Docker:

- Container resource usage

---

## Internal Metrics

Collected using Spring Boot Actuator and Micrometer.

Examples include:

- ratelimiter.requests.allowed
- ratelimiter.requests.blocked
- ratelimiter.redis.failures
- ratelimiter.circuitbreaker.open.transitions
- ratelimiter.request.duration

---

# Benchmark Execution Workflow

Every benchmark follows the same workflow.

```
Restart Machine
        │
        ▼
Verify Docker Environment
        │
        ▼
Start Redis
        │
        ▼
Start Application(s)
        │
        ▼
Warm-up Phase
        │
        ▼
Execute Benchmark
        │
        ▼
Collect Metrics
        │
        ▼
Analyze Results
        │
        ▼
Repeat Benchmark
        │
        ▼
Generate Report
```

---

# Warm-up Policy

Before collecting measurements, the application is warmed up.

The warm-up phase allows:

- JVM class loading
- JIT compilation
- Redis connection establishment
- Spring initialization
- Internal cache population

Warm-up requests are excluded from benchmark results.

---

# Benchmark Categories

The benchmarking phase consists of multiple benchmark categories.

## 1. Baseline Benchmark

Purpose:

Measure application performance under light load.

Metrics:

- Baseline latency
- Baseline throughput
- CPU usage
- Memory usage

---

## 2. Correctness Benchmark

Purpose:

Verify that concurrent requests never exceed configured limits.

Example:

Limit:

```
10 requests
```

Concurrent requests:

```
100 requests
```

Expected outcome:

```
Allowed: 10

Blocked: 90
```

---

## 3. Throughput Benchmark

Purpose:

Measure sustained request processing capacity.

Workload increases gradually until throughput plateaus or latency degrades significantly.

---

## 4. Horizontal Scaling Benchmark

Purpose:

Compare performance between:

- Single application instance
- Three application instances

The objective is to evaluate distributed scalability and shared Redis performance.

---

## 5. Algorithm Comparison Benchmark

Each implemented algorithm is evaluated independently using identical workloads.

Algorithms:

- Token Bucket
- Sliding Window Counter
- Fixed Window

Metrics are compared to evaluate trade-offs in latency, throughput, and resource usage.

---

## 6. Stress Benchmark

Purpose:

Gradually increase system load until resource saturation occurs.

Observations include:

- Throughput degradation
- Latency growth
- Error rate
- Redis bottlenecks
- CPU saturation

---

## 7. Failure Benchmark

Purpose:

Evaluate resilience during Redis failures.

Procedure:

- Execute benchmark
- Interrupt Redis
- Observe circuit breaker
- Observe fail-open or fail-closed behavior
- Restore Redis
- Observe recovery

---

# Tools

The following tools are used during benchmarking.

| Tool | Purpose |
|------|---------|
| k6 | Load generation |
| Docker Desktop | Container execution |
| Redis | Distributed state |
| Spring Boot Actuator | Runtime metrics |
| Micrometer | Application metrics |
| Docker Stats | Resource monitoring |

---

# Benchmark Reporting

Each benchmark report contains:

- Benchmark objective
- Environment
- Configuration
- Workload profile
- Results
- Resource utilization
- Analysis
- Observations
- Conclusions

---

# Statistical Validity

To reduce measurement noise:

- Every benchmark is executed at least three times.
- Results are averaged unless individual runs are explicitly reported.
- Outliers are investigated before inclusion in the final report.

---

# Scope

The benchmarking phase evaluates the Distributed Rate Limiter under a controlled development environment.

It is intended to compare architectural decisions, algorithms, and scalability characteristics rather than simulate a production deployment.

---

# Expected Deliverables

At the completion of the benchmarking phase, the repository will include:

- Benchmark environment documentation
- Benchmark methodology
- Baseline measurements
- Throughput measurements
- Horizontal scaling comparison
- Algorithm comparison
- Stress test results
- Failure test results
- Performance graphs
- Final benchmark summary