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

# Benchmark Framework

This directory contains the complete benchmarking framework for the Distributed Rate Limiter.

## Structure

configs/
Benchmark configurations.

lib/
Shared benchmark utilities.

scenarios/
k6 benchmark scenarios.

results/
Raw benchmark outputs.

reports/
Processed benchmark datasets.

graphs/
Generated benchmark figures.

scripts/
Automation scripts.

## Benchmark Workflow

Validation

↓

Infrastructure Benchmark

↓

Behavioral Benchmark

↓

Distributed Benchmark

↓

Stress Benchmark

## Generate Graphs

```bash
python benchmark/scripts/generate_graphs.py
```

## Reports

See:

```
docs/benchmark/
```

for complete benchmark documentation.
This benchmark suite is intentionally incremental. Each scenario answers one specific engineering question and builds upon the previous scenarios.