# Infrastructure Benchmark

## Overview

The infrastructure benchmark evaluates the baseline performance characteristics of the Distributed Rate Limiter under increasing concurrent load while intentionally preventing rate limiting itself from becoming the bottleneck.

Unlike behavioral benchmarks, this benchmark is **not** intended to verify rate limiting correctness under restrictive limits. Instead, the benchmark measures the intrinsic overhead of the complete request processing pipeline, including:

- HTTP request processing
- RateLimitFilter execution
- Policy resolution
- Registry lookup
- Redis-backed Lua execution
- Response generation

To isolate infrastructure performance, the benchmark uses an extremely high rate limit so that requests are continuously allowed throughout the test.

This benchmark establishes the baseline performance of the system before evaluating realistic rate limiting behavior.

---

# Objectives

The objectives of this benchmark are:

- Measure request throughput under increasing concurrency.
- Measure average and tail latency.
- Verify Redis-backed execution during every request.
- Verify that the benchmark exercises the production code path.
- Confirm that no Redis failures occur.
- Confirm that the Circuit Breaker remains closed.
- Establish a reproducible performance baseline for future comparisons.

---

# Test Environment

The complete benchmark environment is documented in:

- `00-environment.md`

Summary:

| Component | Value |
|-----------|------|
| Operating System | Windows 11 |
| CPU | Intel Core Ultra 5 235U |
| Memory | 16 GB |
| Java | OpenJDK 21 |
| Spring Boot | 4.1 |
| Redis | Redis 7 (Docker) |
| Docker | Docker Desktop (WSL2 Backend) |
| Benchmark Tool | Grafana k6 |

---

# Benchmark Configuration

## Scenario

Infrastructure Benchmark

Purpose:

Measure framework overhead without request rejection.

---

## Rate Limit Configuration

| Property | Value |
|-----------|------|
| Algorithm | Token Bucket |
| Limit | 100000 requests |
| Window | 1 minute |

The configured limit is intentionally large so that rate limiting never becomes the dominant factor during the benchmark.

---

## Benchmark Scenario

Executor:

```
constant-vus
```

Duration:

```
30 seconds
```

Thresholds:

```
http_req_failed < 1%

p95 < 100 ms
```

---

# Validation Before Benchmark

Before collecting official benchmark results, the following validation steps were executed.

- Redis connectivity verified.
- Redis Lua execution verified.
- HTTP response headers verified.
- Remaining token count verified.
- Redis key creation verified.
- Metrics verified.
- Redis failure count verified.
- Benchmark correctness validated.

A previous validation benchmark discovered an infrastructure issue where Redis was unreachable due to Docker port mapping after a system restart.

Because the project uses a FAIL_OPEN resilience strategy, requests continued succeeding while silently bypassing Redis.

The issue was identified before any benchmark numbers were published.

After restoring Docker networking, Redis execution was successfully verified.

This validation process ensured that all subsequent benchmark results represent actual Redis-backed execution rather than fallback behavior.

---

# Benchmark Procedure

For every benchmark run:

1. Start Docker Desktop.
2. Start Redis container.
3. Start Spring Boot application using the benchmark profile.
4. Verify Redis connectivity.
5. Flush Redis.
6. Warm the JVM.
7. Execute benchmark.
8. Capture benchmark results.
9. Capture Actuator metrics.
10. Store benchmark artifacts.

The same procedure was repeated for every concurrency level.

---

# Benchmark Matrix

| Virtual Users |
|--------------|
| 5 |
| 10 |
| 25 |
| 50 |
| 100 |

Each benchmark executed independently.

---

# Results

| Virtual Users | Throughput (req/sec) | Average Latency | P95 Latency | HTTP Failures |
|---------------|----------------------|-----------------|-------------|---------------|
| 5             | 851                  | 5.58 ms         | 9.60 ms     | 0%            |
| 10            | 1267                 | 7.58 ms         | 15.06 ms    | 0%            |
| 25            | 1382                 | 17.72 ms        | 32.01 ms    | 0%            |
| 50            | 1463                 | 33.73 ms        | 67.47 ms    | 0%            |
| 100           | 1555                 | 63.92 ms        | 102.63 ms   | 0%            |

---

# Redis Verification

During every benchmark:

- Redis failures remained zero.
- Lua scripts executed successfully.
- Bucket keys were created.
- Remaining token counts decreased correctly.
- No FAIL_OPEN fallback occurred.

This confirms that the benchmark measured the complete distributed rate limiting implementation rather than a degraded execution path.

---

# Observations

## Throughput

Throughput increased as concurrency increased.

The largest improvement occurred between 5 and 10 virtual users.

Additional concurrency continued increasing throughput but with progressively smaller gains.

This indicates the application gradually approached resource saturation.


![Throughput](../../benchmark/graphs/single-node/throughput-vs-vus.png)

---

## Latency

Average latency increased approximately proportionally with concurrency.

Even at the highest tested concurrency, latency remained well below one hundred milliseconds on average.

Tail latency (P95) also increased gradually as expected.

No abnormal latency spikes or instability were observed.


## Average Latency

![Average Latency](../../benchmark/graphs/single-node/avg-latency-vs-vus.png)

---

## Reliability

Throughout every benchmark:

- Zero HTTP failures occurred.
- Zero Redis failures occurred.
- Zero unexpected request blocks occurred.
- Circuit Breaker remained closed.

The application remained stable throughout all benchmark executions.

---

# Scaling Characteristics

The benchmark demonstrates a typical scalability pattern.

Initially, additional concurrency significantly improves throughput because more requests can be processed simultaneously.

As concurrency increases further, shared resources begin limiting scalability.

Consequently:

- Throughput growth slows.
- Latency continues increasing.
- Additional virtual users provide diminishing throughput improvements.

This behaviour is expected for a Redis-backed application executing Lua scripts for every request.

---


## P95 Latency

![P95](../../benchmark/graphs/single-node/p95-latency-vs-vus.png)

---

## Scaling Efficiency

![Scaling](../../benchmark/graphs/single-node/scaling-efficiency.png)
---


# Limitations

This benchmark intentionally measures a single-node deployment.

The following limitations apply:

- Localhost networking
- Redis hosted on the same machine
- Single JVM
- Laptop hardware
- No TLS
- No external network latency
- One benchmark run per concurrency level

Future benchmark phases will evaluate:

- Distributed deployments
- Multiple application instances
- Cross-instance consistency
- Failure scenarios
- Stress testing

---

# Conclusions

The infrastructure benchmark successfully established a reproducible baseline for the Distributed Rate Limiter.

Key findings include:

- Successful Redis-backed execution.
- Zero Redis failures.
- Zero HTTP failures.
- Stable request processing.
- Predictable latency growth.
- Throughput scaled to approximately 1.5k requests per second on the benchmark hardware.

The benchmark confirms that the implementation is stable and provides a reliable foundation for subsequent behavioral, distributed, and stress benchmarks.