# Benchmark Environment

## Purpose

This document records the complete hardware, software, and execution environment used for all performance benchmarks of the Distributed Rate Limiter.

Documenting the benchmark environment is essential for ensuring that benchmark results are reproducible, comparable, and meaningful. Every benchmark reported in this repository references this environment unless explicitly stated otherwise.

---

# Benchmark Philosophy

Performance benchmarks are only valuable when they can be reproduced under similar conditions.

Rather than simply reporting throughput or latency numbers, this project documents:

- Hardware specifications
- Operating system
- Runtime configuration
- Docker configuration
- WSL2 resource allocation
- Software versions
- Benchmark execution rules

This approach allows future benchmarks to be compared against a consistent baseline.

---

# Hardware Specifications

| Component | Specification |
|------------|--------------|
| CPU | Intel® Core™ Ultra 5 235U |
| Physical Cores | 12 |
| Logical Processors | 14 |
| Installed RAM | 16 GB |
| Storage | 256 GB SSD |
| Operating System | Windows 11 |

---

# WSL2 Configuration

Docker Desktop uses the WSL2 backend.

To provide a stable and reproducible benchmark environment, WSL2 resource limits were explicitly configured.

### `.wslconfig`

```ini
[wsl2]
memory=8GB
processors=8
swap=2GB
localhostForwarding=true
```

### Resource Allocation

| Resource | Value |
|----------|------:|
| Memory | 8 GB |
| Processors | 8 |
| Swap | 2 GB |

Rationale:

- Reserve sufficient resources for Docker workloads.
- Leave adequate CPU and memory for the Windows host.
- Improve benchmark consistency across repeated executions.
- Reduce variability caused by dynamic resource allocation.

---

# Java Environment

| Component | Version |
|-----------|---------|
| Java | Oracle JDK 21.0.11 LTS |
| Maven | 3.9.16 |
| Spring Boot | 4.1.0 |

Although the project targets Java 17 bytecode, benchmarks are executed using Oracle JDK 21 LTS.

---

# Docker Environment

| Component | Version |
|-----------|---------|
| Docker Desktop | 4.79.0 |
| Docker Engine | 29.5.3 |
| Docker Compose | 5.1.4 |
| Docker Backend | WSL2 |

---

# Redis Environment

| Component | Version |
|-----------|---------|
| Redis | 7-alpine |

Redis acts as the shared distributed state store for all benchmark scenarios.

---

# Project Images

| Image | Purpose |
|-------|---------|
| distributed-rate-limiter | Spring Boot application |
| redis:7-alpine | Shared Redis instance |

---

# Docker Deployment Topology

```
                  +----------------------+
                  |      Docker Host     |
                  +----------------------+

        +---------+---------+---------+
        |                   |         |
      App1                App2      App3
       |                   |         |
       +---------+---------+---------+
                         |
                     Redis 7
```

All application instances communicate with a single shared Redis instance.

---

# Power Configuration

During benchmarking:

- Windows Power Mode is set to **Best Performance**.
- Docker Resource Saver is disabled.
- No unnecessary background applications remain open.

---

# Benchmark Session Preparation

Before every benchmark session:

- Restart the system.
- Start Docker Desktop.
- Verify Redis is healthy.
- Verify all application containers are running.
- Wait until CPU utilization stabilizes.
- Close browsers, IDEs (except IntelliJ), messaging applications, game launchers, and background software unrelated to benchmarking.
- Do not perform project builds during benchmark execution.

---

# Benchmark Environment Rules

Every official benchmark reported in this repository follows these rules:

- Execute using the same hardware configuration.
- Execute using identical software versions.
- Use the same Docker configuration.
- Use the same WSL2 resource limits.
- Run each benchmark at least three times.
- Average the results unless otherwise stated.
- Record benchmark date and Git commit.

---

# Environment Limitations

Benchmark results collected in this repository represent performance on a single developer workstation.

They should be interpreted as relative measurements for comparing algorithms, scalability, and architectural decisions rather than absolute production capacity.

Production deployments may achieve different throughput depending on:

- Hardware
- Network latency
- Redis deployment topology
- JVM tuning
- Operating system
- Container orchestration platform

---

# Environment Revision History

| Date | Change |
|------|--------|
| Initial Benchmark Phase | Created benchmark environment documentation |