param(

    [ValidateSet(
        "TOKEN_BUCKET",
        "SLIDING_WINDOW_COUNTER",
        "FIXED_WINDOW"
    )]
    [string]$Algorithm = "TOKEN_BUCKET",

    [int]$Limit = 100,

    [string]$Window = "1m",

    [switch]$SkipRestart

)

# ----------------------------------------------------------
# Bootstrap
# ----------------------------------------------------------

. "$PSScriptRoot/../common/Bootstrap.ps1"

$deployment = "Distributed"

$config = Get-DeploymentConfiguration `
            -Deployment $deployment

$resultDirectory = Join-Path `
    $config.ResultDirectory `
    "validation/$Algorithm/limit-$Limit"

Write-Host ""
Write-Host "==============================================="
Write-Host "Distributed Validation Benchmark"
Write-Host "==============================================="
Write-Host ""

Write-Host "Deployment : $deployment"
Write-Host "Algorithm  : $Algorithm"
Write-Host "Limit      : $Limit"
Write-Host "Window     : $Window"
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

# ----------------------------------------------------------
# Execute Benchmark
# ----------------------------------------------------------

Invoke-K6Benchmark `
    -Deployment $deployment `
    -Scenario "01-validation.js" `
    -ResultDirectory $resultDirectory `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window `
    -VUs 1 `
    -Duration "1s" | Out-Null

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
    -VUs 1 `
    -Duration "1s"

# ----------------------------------------------------------
# Console Summary
# ----------------------------------------------------------

Write-Host ""
Write-Host "==============================================="
Write-Host "Validation Completed"
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