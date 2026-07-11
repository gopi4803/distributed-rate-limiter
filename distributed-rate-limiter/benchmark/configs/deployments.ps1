# ==========================================================
# Distributed Rate Limiter
# Benchmark Deployment Configuration
# ==========================================================

$BenchmarkDeployments = @{

    SingleNode = @{

        Name = "Single Node"

        ProjectName = "drl-single"

        ComposeFile = "docker/docker-compose.single-node.yml"

        BaseUrl = "http://localhost:8080"

        HealthEndpoint = "/actuator/health"

        MetricsEndpoint = "/actuator/metrics"

        BenchmarkEndpoint = "/api/v1/rate-limit/benchmark"

        ResultDirectory = "benchmark/results/single-node"

        ReportDirectory = "benchmark/reports/single-node"

        GraphDirectory = "benchmark/graphs/single-node"

        Instances = @(
            @{
                Name = "app"
                Url = "http://localhost:8080"
            }
        )
    }

    Distributed = @{

        Name = "Distributed"

        ProjectName = "drl-distributed"

        ComposeFile = "docker/docker-compose.distributed.yml"

        BaseUrl = "http://localhost"

        HealthEndpoint = "/actuator/health"

        MetricsEndpoint = "/actuator/metrics"

        BenchmarkEndpoint = "/api/v1/rate-limit/benchmark"

        ResultDirectory = "benchmark/results/distributed"

        ReportDirectory = "benchmark/reports/distributed"

        GraphDirectory = "benchmark/graphs/distributed"

        Instances = @(
            @{
                Name = "app1"
                Url = "http://localhost:8080"
            },
            @{
                Name = "app2"
                Url = "http://localhost:8081"
            },
            @{
                Name = "app3"
                Url = "http://localhost:8082"
            }
        )
    }

}