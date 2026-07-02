# Benchmark Suite

## Purpose

This directory contains the complete performance benchmarking suite for the Distributed Rate Limiter.

The benchmark suite is designed to measure:

- Throughput
- Latency
- Scalability
- Resource utilization
- Distributed correctness
- Failure behavior

Benchmarks are executed using **k6**.

---

## Directory Structure

```text
benchmark/
│
├── scenarios/
├── configs/
├── results/
├── reports/
└── graphs/
```

---

## Scenarios

| Scenario | Purpose |
|----------|---------|
| baseline | Validate benchmark environment and establish baseline |
| correctness | Validate rate limiting correctness |
| throughput | Measure sustained throughput |
| scaling | Compare single-node and multi-node deployments |
| stress | Identify system saturation point |
| failure | Evaluate resilience during Redis failures |

---

This benchmark suite is intentionally incremental. Each scenario answers one specific engineering question and builds upon the previous scenarios.