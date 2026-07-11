# ==========================================================
# Distributed Rate Limiter
# Docker Utilities
# ==========================================================

function Start-BenchmarkDeployment {

    param(
        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment
    )

    $config = Get-DeploymentConfiguration $Deployment

    Write-Host ""
    Write-Host "========================================="
    Write-Host "Starting $($config.Name)"
    Write-Host "========================================="
    Write-Host ""

    docker compose `
        -p $config.ProjectName `
        -f $config.ComposeFile `
        up `
        --build `
        -d

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to start deployment."
    }

    Wait-ForDeploymentHealth -Deployment $Deployment
}

function Stop-BenchmarkDeployment {

    param(
        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment
    )

    $config = Get-DeploymentConfiguration $Deployment

    Write-Host ""
    Write-Host "Stopping $($config.Name)..."
    Write-Host ""

    docker compose `
        -p $config.ProjectName `
        -f $config.ComposeFile `
        down `
        --remove-orphans

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to stop deployment."
    }
}

function Restart-BenchmarkDeployment {

    param(
        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment
    )

    Stop-BenchmarkDeployment -Deployment $Deployment

    Start-Sleep -Seconds 2

    Start-BenchmarkDeployment -Deployment $Deployment
}