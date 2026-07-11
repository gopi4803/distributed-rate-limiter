# ==========================================================
# Shared Benchmark Configuration
# ==========================================================

$BenchmarkConfiguration = @{

    EnvironmentFile = "docker/benchmark.env"

    RedisService = "redis"

    NginxService = "nginx"

    BenchmarkProfile = "benchmark"

    GraphScript = "benchmark/scripts/generate_graphs.py"

    BehavioralGraphScript = "benchmark/scripts/generate_behavioral_graphs.py"

}