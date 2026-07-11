# ==========================================================
# Distributed Rate Limiter
# Benchmark Runner
# ==========================================================

# ==========================================================
# Benchmark Defaults
# ==========================================================

$DefaultWarmupVUs = 5
$DefaultWarmupDuration = "5s"

# ----------------------------------------------------------
# Invoke K6 Benchmark
# ----------------------------------------------------------

function Invoke-K6Benchmark {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [string]$Scenario,

        [Parameter(Mandatory)]
        [string]$ResultDirectory,

        [Parameter(Mandatory)]
        [ValidateSet(
            "TOKEN_BUCKET",
            "SLIDING_WINDOW_COUNTER",
            "FIXED_WINDOW"
        )]
        [string]$Algorithm,

        [Parameter(Mandatory)]
        [int]$Limit,

        [Parameter(Mandatory)]
        [string]$Window,

        [Parameter(Mandatory)]
        [int]$VUs,

        [Parameter(Mandatory)]
        [string]$Duration

    )

    Ensure-Directory `
        -Path $ResultDirectory

    $config = Get-DeploymentConfiguration `
                -Deployment $Deployment

    $summaryFile = Join-Path `
        $ResultDirectory `
        "$($VUs)vu-summary.json"

    $scenarioFile = Join-Path `
        (Get-BenchmarkDirectory) `
        "scenarios/$Scenario"

    $liveMetricsFile = Join-Path `
        $ResultDirectory `
        "docker-live-metrics.csv"

    Write-Host ""
    Write-Host "==============================================="
    Write-Host "Running k6 Benchmark"
    Write-Host "==============================================="
    Write-Host ""

    Write-Host "Scenario : $Scenario"
    Write-Host "Algorithm: $Algorithm"
    Write-Host "Limit    : $Limit"
    Write-Host "Window   : $Window"
    Write-Host "VUs      : $VUs"
    Write-Host "Duration : $Duration"
    Write-Host ""

    $dockerSamplingJob = Start-DockerMetricsSampling `
        -Deployment $Deployment `
        -OutputFile $liveMetricsFile

    $exitCode = -1

    try {

        k6 run `
            -e BASE_URL=$($config.BaseUrl) `
            -e BENCHMARK_ALGORITHM=$Algorithm `
            -e BENCHMARK_LIMIT=$Limit `
            -e BENCHMARK_WINDOW=$Window `
            -e VUS=$VUs `
            -e DURATION=$Duration `
            --summary-export $summaryFile `
            $scenarioFile

        $exitCode = $LASTEXITCODE

    }
    finally {

        Stop-DockerMetricsSampling `
            -Job $dockerSamplingJob

    }

    switch ($exitCode) {

        0 {

            Write-Host ""
            Write-Host "[OK] Benchmark completed successfully."
            Write-Host ""

        }

        99 {

            Write-Warning ""
            Write-Warning "One or more k6 thresholds were exceeded."
            Write-Warning "Benchmark results have still been generated."
            Write-Warning "Continuing with metrics collection..."
            Write-Warning ""

        }

        default {

            throw "k6 execution failed with exit code $exitCode."

        }

    }

    return $summaryFile

}

function Invoke-BenchmarkWarmup {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [ValidateSet(
            "TOKEN_BUCKET",
            "SLIDING_WINDOW_COUNTER",
            "FIXED_WINDOW"
        )]
        [string]$Algorithm,

        [Parameter(Mandatory)]
        [int]$Limit,

        [Parameter(Mandatory)]
        [string]$Window,

        [int]$VUs = $DefaultWarmupVUs,

        [string]$Duration = $DefaultWarmupDuration

    )

    $config = Get-DeploymentConfiguration `
                -Deployment $Deployment

    $scenarioFile = Join-Path `
        (Get-BenchmarkDirectory) `
        "scenarios/02-infrastructure.js"

    Write-Host ""
    Write-Host "==============================================="
    Write-Host "Warm-up Phase"
    Write-Host "==============================================="
    Write-Host ""

    k6 run `
        --quiet `
        -e BASE_URL=$($config.BaseUrl) `
        -e BENCHMARK_ALGORITHM=$Algorithm `
        -e BENCHMARK_LIMIT=$Limit `
        -e BENCHMARK_WINDOW=$Window `
        -e VUS=$VUs `
        -e DURATION=$Duration `
        $scenarioFile | Out-Null

    if ($LASTEXITCODE -ne 0) {
        throw "Warm-up failed."
    }

    Write-Host "[OK] Warm-up completed."
    Write-Host ""

}

# ----------------------------------------------------------
# Collect Benchmark Metrics
# ----------------------------------------------------------

function Collect-BenchmarkMetrics {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    Write-Host ""
    Write-Host "==============================================="
    Write-Host "Collecting Benchmark Metrics"
    Write-Host "==============================================="
    Write-Host ""

    Write-Host "Collecting Spring Metrics..."

    $springMetrics = Get-AllSpringMetrics `
                        -Deployment $Deployment

    Write-Host "Collecting Redis Information..."

    $redisInfo = Get-RedisInfo `
                    -Deployment $Deployment

    $redisMemory = Get-RedisMemoryInfo `
                        -Deployment $Deployment

    $redisStats = Get-RedisStats `
                        -Deployment $Deployment

    Write-Host "Collecting Docker Metrics..."

    $dockerMetrics = Get-AllDockerMetrics `
                        -Deployment $Deployment

    return [PSCustomObject]@{

        Timestamp = Get-Date

        Deployment = $Deployment

        Spring = $springMetrics

        Redis = [PSCustomObject]@{

            Info = $redisInfo

            Memory = $redisMemory

            Stats = $redisStats

        }

        Docker = $dockerMetrics

    }

}

# ----------------------------------------------------------
# Save Benchmark Artifacts
# ----------------------------------------------------------

function Save-BenchmarkArtifacts {

    param(

        [Parameter(Mandatory)]
        $Metrics,

        [Parameter(Mandatory)]
        [string]$ResultDirectory,

        [Parameter(Mandatory)]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [ValidateSet(
            "TOKEN_BUCKET",
            "SLIDING_WINDOW_COUNTER",
            "FIXED_WINDOW"
        )]
        [string]$Algorithm,

        [Parameter(Mandatory)]
        [int]$Limit,

        [Parameter(Mandatory)]
        [string]$Window,

        [Parameter(Mandatory)]
        [int]$VUs,

        [Parameter(Mandatory)]
        [string]$Duration

    )

    Ensure-Directory `
        -Path $ResultDirectory

    Write-Host ""
    Write-Host "==============================================="
    Write-Host "Saving Benchmark Reports"
    Write-Host "==============================================="
    Write-Host ""

    #
    # Spring Metrics
    #

    Save-JsonReport `
        -Data $Metrics.Spring `
        -OutputFile (Join-Path $ResultDirectory "spring-metrics.json")

    #
    # Redis
    #

    Save-JsonReport `
        -Data $Metrics.Redis.Info `
        -OutputFile (Join-Path $ResultDirectory "redis-info.json")

    Save-JsonReport `
        -Data $Metrics.Redis.Memory `
        -OutputFile (Join-Path $ResultDirectory "redis-memory.json")

    Save-JsonReport `
        -Data $Metrics.Redis.Stats `
        -OutputFile (Join-Path $ResultDirectory "redis-stats.json")

    #
    # Docker
    #

    Save-JsonReport `
        -Data $Metrics.Docker `
        -OutputFile (Join-Path $ResultDirectory "docker-metrics.json")

    Save-CsvReport `
        -Data $Metrics.Docker.ResourceUsage `
        -OutputFile (Join-Path $ResultDirectory "docker-resource-usage.csv")

    Save-CsvReport `
        -Data $Metrics.Docker.ContainerStatus `
        -OutputFile (Join-Path $ResultDirectory "docker-container-status.csv")

    #
    # Benchmark Summary
    #

    $summary = [PSCustomObject]@{

        Timestamp = Get-Date

        Deployment = $Deployment

        Algorithm = $Algorithm

        Limit = $Limit

        Window = $Window

        VirtualUsers = $VUs

        Duration = $Duration

    }

    Save-BenchmarkSummary `
        -Summary $summary `
        -OutputFile (Join-Path $ResultDirectory "benchmark-summary.json")

}
