param(

    [ValidateSet(
        "TOKEN_BUCKET",
        "SLIDING_WINDOW_COUNTER",
        "FIXED_WINDOW"
    )]
    [string]$Algorithm = "TOKEN_BUCKET",

    [int]$Limit = 1000000,

    [string]$Window = "1m",

    [int]$VUs = 10,

    [string]$Duration = "30s",

    [switch]$SkipRestart

)

# ----------------------------------------------------------
# Bootstrap
# ----------------------------------------------------------

. "$PSScriptRoot/../common/Bootstrap.ps1"

$deployment = "SingleNode"

$config = Get-DeploymentConfiguration `
            -Deployment $deployment

$resultDirectory = Join-Path `
    $config.ResultDirectory `
    "infrastructure/$($VUs)vu"

Write-Host ""
Write-Host "==============================================="
Write-Host "Infrastructure Benchmark"
Write-Host "==============================================="
Write-Host ""

Write-Host "Deployment : $deployment"
Write-Host "Algorithm  : $Algorithm"
Write-Host "Limit      : $Limit"
Write-Host "Window     : $Window"
Write-Host "VUs        : $VUs"
Write-Host "Duration   : $Duration"
Write-Host ""

# ----------------------------------------------------------
# Configure Benchmark
# ----------------------------------------------------------

Set-BenchmarkConfiguration `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window

# ----------------------------------------------------------
# Restart Deployment
# ----------------------------------------------------------

if (-not $SkipRestart) {

    Write-Host ""
    Write-Host "Restarting deployment..."
    Write-Host ""

    Restart-BenchmarkDeployment `
        -Deployment $deployment

}
else {

    Write-Host ""
    Write-Host "Reusing existing deployment."
    Write-Host ""

}

# ----------------------------------------------------------
# Flush Redis
# ----------------------------------------------------------

Clear-Redis `
    -Deployment $deployment

if (-not $SkipRestart) {

    Invoke-BenchmarkWarmup `
        -Deployment $deployment `
        -Algorithm $Algorithm `
        -Limit $Limit `
        -Window $Window

}

# ----------------------------------------------------------
# Execute Benchmark
# ----------------------------------------------------------

Invoke-K6Benchmark `
    -Deployment $deployment `
    -Scenario "02-infrastructure.js" `
    -ResultDirectory $resultDirectory `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window `
    -VUs $VUs `
    -Duration $Duration | Out-Null

# ----------------------------------------------------------
# Collect Metrics
# ----------------------------------------------------------

$metrics = Collect-BenchmarkMetrics `
                -Deployment $deployment

# ----------------------------------------------------------
# Save Reports
# ----------------------------------------------------------

Save-BenchmarkArtifacts `
    -Metrics $metrics `
    -ResultDirectory $resultDirectory `
    -Deployment $deployment `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window `
    -VUs $VUs `
    -Duration $Duration

# ----------------------------------------------------------
# Console Summary
# ----------------------------------------------------------

Write-Host ""
Write-Host "==============================================="
Write-Host "Infrastructure Benchmark Completed"
Write-Host "==============================================="
Write-Host ""

Write-Host "Results:"
Write-Host "  $resultDirectory"

Write-Host ""

Show-RedisSummary `
    -Deployment $deployment

Show-DockerSummary `
    -Deployment $deployment

Write-Host ""
Write-Host "Done."
Write-Host ""