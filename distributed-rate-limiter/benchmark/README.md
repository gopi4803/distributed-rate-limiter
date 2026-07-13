# Benchmark Framework

The benchmark framework provides a repeatable and automated way to evaluate the **performance**, **correctness**, **scalability**, and **resource utilization** of the Distributed Rate Limiter.

Rather than executing isolated load tests, the framework automates the complete benchmark lifecycle including environment validation, deployment orchestration, workload execution, infrastructure monitoring, report generation, and graph creation.

It supports both **single-node** and **distributed** deployments using identical workloads, enabling consistent performance comparisons across deployment models.

---

# Benchmark Goals

The framework is designed to answer several engineering questions.

* Does the deployment function correctly before benchmarking?
* How much throughput can the system sustain?
* How does latency change as concurrency increases?
* How efficiently does the system scale?
* How do different rate limiting algorithms behave?
* What infrastructure resources are consumed during execution?
* How does distributed deployment compare with a single-node deployment?

---

# Benchmark Architecture

The benchmark framework combines automation, workload generation, infrastructure monitoring, reporting, and graph generation into a single execution pipeline.

```text
                    Benchmark Script
                           │
                           ▼
               PowerShell Automation
                           │
                           ▼
               Environment Validation
                           │
                           ▼
                  Deployment Startup
                           │
                           ▼
                     k6 Workload
                           │
                           ▼
                 Infrastructure Metrics
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
    Docker            Spring Boot          Redis
     Metrics             Metrics          Metrics
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
                  Benchmark Results
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
      Reports          Graphs          Raw Results
```

Each stage is independent, making the framework modular, reusable, and easy to extend.

---

# Directory Structure

```text
benchmark/
│
├── automation/          # PowerShell automation
├── configs/             # Benchmark configuration
├── lib/                 # Shared k6 utilities
├── scenarios/           # Benchmark workloads
├── scripts/             # Report & graph generation
├── graphs/              # Generated visualizations
├── reports/             # Generated reports
├── results/             # Raw benchmark outputs
└── README.md
```

The benchmark module separates execution logic, generated artifacts, and reusable utilities to simplify maintenance and future expansion.

---

# Benchmark Scenarios

The framework consists of three independent benchmark categories.

## Validation

Verifies the benchmark environment before executing performance tests.

Checks include:

* Application availability
* Redis connectivity
* HTTP responses
* Required headers
* Basic rate limiting behaviour

Validation ensures performance measurements are collected only from a healthy deployment.

---

## Infrastructure

Measures deployment performance under increasing concurrent workloads.

Collected metrics include:

* Throughput
* Average latency
* P95 latency
* HTTP failure rate
* Scaling efficiency

Infrastructure benchmarks also collect Docker, Redis, and Spring Boot metrics throughout execution.

---

## Behavioural

Evaluates the runtime characteristics of supported rate limiting algorithms.

Current algorithms:

* Token Bucket
* Sliding Window Counter
* Fixed Window

Each algorithm is executed using identical workloads to enable direct performance comparisons.

---

# Automation Framework

Benchmark execution is fully automated using reusable PowerShell modules.

Major responsibilities include:

* Deployment startup
* Environment validation
* Warm-up execution
* Benchmark execution
* Infrastructure metrics collection
* Report generation
* Graph generation

Automation scripts are shared across both deployment modes, ensuring benchmark consistency.

---

# Automation Components

| Component                  | Responsibility                              |
| -------------------------- | ------------------------------------------- |
| BenchmarkConfiguration.ps1 | Loads and validates benchmark configuration |
| BenchmarkRunner.ps1        | Executes benchmark lifecycle                |
| DockerMetrics.ps1          | Collects Docker metrics during execution    |
| run-all.ps1                | Executes the complete benchmark suite       |
| run-infrastructure.ps1     | Runs infrastructure benchmarks              |
| run-behavioral.ps1         | Runs behavioural benchmarks                 |

---

# Benchmark Execution Flow

Every benchmark follows the same workflow.

```text
Run Benchmark
      │
      ▼
Load Configuration
      │
      ▼
Validate Environment
      │
      ▼
Deploy Application
      │
      ▼
Warm-up Requests
      │
      ▼
Execute k6 Scenario
      │
      ▼
Collect Infrastructure Metrics
      │
      ▼
Generate Results
      │
      ▼
Generate Reports & Graphs
```

Using a consistent execution flow minimizes environmental variability between benchmark runs.

---

# Benchmark Configuration

Benchmark behaviour is configured centrally rather than modifying benchmark scenarios.

Typical configuration options include:

* Deployment mode
* Algorithm selection
* Request limits
* Virtual users
* Benchmark duration
* Warm-up requests
* Output directories

Centralized configuration simplifies repeated benchmark execution while ensuring consistency.

---

# Running Benchmarks

The framework provides dedicated automation for both deployment modes while maintaining a consistent execution workflow.

## Execute the Complete Benchmark Suite

### Single-Node Deployment

```powershell
benchmark/automation/single-node/run-all.ps1
```

### Distributed Deployment

```powershell
benchmark/automation/distributed/run-all.ps1
```

The `run-all` scripts execute the complete benchmark pipeline, including environment validation, workload execution, metrics collection, report generation, and graph generation.

---

## Execute Individual Benchmark Suites

Infrastructure benchmark:

### Single-Node

```powershell
benchmark/automation/single-node/run-infrastructure.ps1
```

### Distributed

```powershell
benchmark/automation/distributed/run-infrastructure.ps1
```

Behavioural benchmark:

### Single-Node

```powershell
benchmark/automation/single-node/run-behavioral.ps1
```

### Distributed

```powershell
benchmark/automation/distributed/run-behavioral.ps1
```

Running individual benchmark suites is useful when validating a specific change without executing the complete benchmark pipeline.

---

# Generated Artifacts

Every benchmark execution produces a collection of artifacts used for analysis, reporting, and visualization.

## Results

Location

```text
benchmark/results/
```

Contains:

* Raw k6 benchmark summaries
* Infrastructure metrics
* Deployment-specific benchmark outputs
* Benchmark metadata

---

## Reports

Location

```text
benchmark/reports/
```

Contains:

* Aggregated benchmark summaries
* Performance statistics
* Human-readable reports

---

## Graphs

Location

```text
benchmark/graphs/
```

Generated visualizations include:

* Throughput vs Virtual Users
* Average Latency vs Virtual Users
* P95 Latency vs Virtual Users
* HTTP Failure Rate
* Scaling Efficiency
* Deployment Comparison Graphs

The generated graphs are committed to the repository and referenced directly from the project documentation.

---

# Metrics Collected

The benchmark framework gathers metrics from multiple sources to provide a complete picture of application and infrastructure behaviour.

| Source          | Metrics                                                        |
| --------------- | -------------------------------------------------------------- |
| **k6**          | Throughput, latency, P95 latency, failure rate, request count  |
| **Docker**      | CPU, memory, network throughput, block I/O                     |
| **Redis**       | Memory usage, connected clients, operational statistics        |
| **Spring Boot** | HTTP metrics, JVM metrics, memory, threads, application health |

Collecting metrics from multiple layers makes it easier to correlate benchmark performance with underlying infrastructure behaviour.

---

# Graph Generation

Performance graphs are generated from benchmark results and can be regenerated without rerunning the benchmarks.

## Infrastructure Graphs

Generate deployment-specific graphs:

```bash
python benchmark/scripts/generate_graphs.py single-node

python benchmark/scripts/generate_graphs.py distributed
```

---

## Behavioural Graphs

Generate algorithm comparison graphs:

```bash
python benchmark/scripts/generate_behavioral_graphs.py
```

---

## Deployment Comparison Graphs

Generate comparison graphs across deployment modes:

```bash
python benchmark/scripts/generate_comparison_graphs.py
```

Comparison graphs include:

* Throughput Comparison
* Average Latency Comparison
* P95 Latency Comparison
* Scaling Efficiency Comparison
* HTTP Failure Rate Comparison

---

# Extending the Framework

The benchmark framework is intentionally modular, allowing new workloads and metrics to be added with minimal changes.

## Adding a New Benchmark Scenario

1. Create a new k6 workload under:

```text
benchmark/scenarios/
```

2. Add shared utilities to:

```text
benchmark/lib/
```

3. Register the scenario in the appropriate automation script.

4. Execute using the existing benchmark framework.

---

## Adding New Metrics

Additional metrics can be incorporated by extending the benchmark scenarios or infrastructure collection scripts.

Examples include:

* JVM garbage collection metrics
* Connection pool statistics
* Redis command latency
* Network throughput
* Custom application metrics

Generated reports and graphs can then be extended to visualize the additional measurements.

---

# Best Practices

To obtain reliable and repeatable benchmark results:

* Execute benchmarks on an otherwise idle machine.
* Perform a warm-up phase before collecting measurements. (A warm-up phase is executed before measurement to reduce cold-start effects and improve benchmark repeatability.)
* Execute multiple runs and compare averaged results.
* Use identical benchmark configurations when comparing deployments.
* Keep generated artifacts together for traceability.
* Restart deployments between benchmark suites when appropriate.

Following these practices reduces environmental variability and improves confidence in benchmark comparisons.

---

# Documentation

Additional benchmark documentation is available throughout the repository.

| Location              | Description                                 |
| --------------------- | ------------------------------------------- |
| `README.md`           | Project overview                            |
| `benchmark/README.md` | Benchmark framework                         |
| `docs/README.md`      | Documentation index                         |
| `docs/benchmark/`     | Benchmark methodology and detailed analysis |

The root README summarizes benchmark highlights, while the documentation under `docs/benchmark/` provides detailed explanations of methodology, environment configuration, infrastructure metrics, and performance analysis.

---

# Future Enhancements

The framework has been designed with future extensibility in mind.

Potential improvements include:

* Kubernetes benchmark automation
* Redis Cluster benchmarking
* Cloud deployment benchmarks
* Prometheus and Grafana integration
* OpenTelemetry metrics
* CI/CD benchmark execution
* Historical benchmark trend analysis
* Automated performance regression detection

---
