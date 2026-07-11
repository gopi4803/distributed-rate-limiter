param(

    [ValidateSet(
        "TOKEN_BUCKET",
        "SLIDING_WINDOW_COUNTER",
        "FIXED_WINDOW"
    )]
    [string]$Algorithm = "TOKEN_BUCKET",

    # Used for validation + behavioral benchmarks
    [int]$Limit = 100,

    [string]$Window = "1m"

)

# ----------------------------------------------------------
# Bootstrap
# ----------------------------------------------------------

. "$PSScriptRoot/../common/Bootstrap.ps1"

$deployment = "Distributed"

$config = Get-DeploymentConfiguration -Deployment $deployment

$InfrastructureLimit = 1000000

$infrastructureVUs = @(5,10,25,50,100)

$behavioralVUs = @(5,50,100)

Write-Host ""
Write-Host "===================================================="
Write-Host "Distributed Benchmark Suite"
Write-Host "===================================================="
Write-Host ""

Write-Host "Deployment          : $deployment"
Write-Host "Algorithm           : $Algorithm"
Write-Host "Behavioral Limit    : $Limit"
Write-Host "Infrastructure Limit: $InfrastructureLimit"
Write-Host "Window              : $Window"
Write-Host ""

#
# Configure once for validation
#

Set-BenchmarkConfiguration `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window

Restart-BenchmarkDeployment `
    -Deployment $deployment

#
# Validation
#

Write-Host ""
Write-Host "Running Validation Benchmark..."
Write-Host ""

& "$PSScriptRoot/run-validation.ps1" `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window `
    -SkipRestart

#
# Infrastructure
#

foreach($vu in $infrastructureVUs){

    Write-Host ""
    Write-Host "Running Infrastructure Benchmark ($vu VUs)..."
    Write-Host ""

    & "$PSScriptRoot/run-infrastructure.ps1" `
        -Algorithm $Algorithm `
        -Limit $InfrastructureLimit `
        -Window $Window `
        -VUs $vu `
        -Duration "30s" `
        -SkipRestart
}

#
# Behavioral
#

foreach($vu in $behavioralVUs){

    Write-Host ""
    Write-Host "Running Behavioral Benchmark ($vu VUs)..."
    Write-Host ""

    & "$PSScriptRoot/run-behavioral.ps1" `
        -Algorithm $Algorithm `
        -Limit $Limit `
        -Window $Window `
        -VUs $vu `
        -Duration "30s"

}

#
# Generate Graphs
#

Write-Host ""
Write-Host "Generating Graphs..."
Write-Host ""

python (Get-GraphScript) "distributed"

New-BenchmarkReport `
    -Deployment $deployment `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window

if($LASTEXITCODE -ne 0){

    Write-Warning "Graph generation failed."

}

Write-Host ""
Write-Host "===================================================="
Write-Host "Benchmark Suite Completed"
Write-Host "===================================================="
Write-Host ""

Write-Host "Reports:"
Write-Host "  $($config.ReportDirectory)"

Write-Host ""

Write-Host "Results:"
Write-Host "  $($config.ResultDirectory)"

Write-Host ""

Write-Host "Graphs:"
Write-Host "  $($config.GraphDirectory)"

Write-Host ""