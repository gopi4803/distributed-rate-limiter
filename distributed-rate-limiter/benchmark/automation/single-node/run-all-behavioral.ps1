param(

    [ValidateSet(
        "TOKEN_BUCKET",
        "SLIDING_WINDOW_COUNTER",
        "FIXED_WINDOW"
    )]
    [string]$Algorithm = "TOKEN_BUCKET",

    [int]$Limit = 100,

    [string]$Window = "1m"

)

. "$PSScriptRoot/../common/Bootstrap.ps1"

$deployment = "SingleNode"

$vusList = @(5, 50, 100)

Write-Host ""
Write-Host "==============================================="
Write-Host "Behavioral Benchmark Suite"
Write-Host "==============================================="
Write-Host ""

Write-Host "Deployment : $deployment"
Write-Host "Algorithm  : $Algorithm"
Write-Host "Limit      : $Limit"
Write-Host "Window     : $Window"

Write-Host ""

#
# Configure benchmark once
#

Set-BenchmarkConfiguration `
    -Algorithm $Algorithm `
    -Limit $Limit `
    -Window $Window

Restart-BenchmarkDeployment `
    -Deployment $deployment

foreach($vu in $vusList){

    & "$PSScriptRoot/run-behavioral.ps1" `
        -Algorithm $Algorithm `
        -Limit $Limit `
        -Window $Window `
        -VUs $vu `
        -Duration "30s" `
        -SkipRestart

}

Write-Host ""
Write-Host "==============================================="
Write-Host "Behavioral Benchmark Suite Completed"
Write-Host "==============================================="
Write-Host ""