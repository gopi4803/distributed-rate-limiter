# ==========================================================
# Distributed Rate Limiter
# Docker Metrics Utilities
# ==========================================================

# ----------------------------------------------------------
# Get Docker Containers
# ----------------------------------------------------------

function Get-DockerContainers {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $config = Get-DeploymentConfiguration -Deployment $Deployment

    $jsonLines = docker compose `
                    -p $config.ProjectName `
                    -f $config.ComposeFile `
                    ps `
                    --format json

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to retrieve Docker containers."
    }

    $results = @()

    foreach($line in $jsonLines){

        if([string]::IsNullOrWhiteSpace($line)){
            continue
        }

        $results += ($line | ConvertFrom-Json)

    }

    return $results

}

# ----------------------------------------------------------
# Get Container Status
# ----------------------------------------------------------

function Get-ContainerStatus {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $containers = Get-DockerContainers -Deployment $Deployment

    $results = @()

    foreach($container in $containers){

        $results += [PSCustomObject]@{

            Timestamp  = Get-Date

            Deployment = $Deployment

            Service    = $container.Service

            Name       = $container.Name

            ContainerId = $container.ID

            Image      = $container.Image

            State      = $container.State

            Status     = $container.Status

            Health     = $container.Health

            ExitCode   = $container.ExitCode

            Networks   = $container.Networks

            Ports      = $container.Ports

        }

    }

    return $results

}

# ----------------------------------------------------------
# Get Container Resource Usage
# ----------------------------------------------------------

function Get-ContainerResourceUsage {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $statsOutput = docker stats `
                        --no-stream `
                        --format "{{json .}}"

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to retrieve Docker statistics."
    }

    $statsLookup = @{}

    foreach($line in $statsOutput){

        if([string]::IsNullOrWhiteSpace($line)){
            continue
        }

        $stat = $line | ConvertFrom-Json

        $statsLookup[$stat.Name] = $stat

    }

    $containers = Get-DockerContainers -Deployment $Deployment

    $results = @()

    foreach($container in $containers){

        $stat = $statsLookup[$container.Name]

        if($null -eq $stat){
            continue
        }

        $results += [PSCustomObject]@{

            Timestamp     = Get-Date

            Deployment    = $Deployment

            Service       = $container.Service

            Name          = $container.Name

            ContainerId   = $container.ID

            CPUPercent    = $stat.CPUPerc

            MemoryUsage   = $stat.MemUsage

            MemoryPercent = $stat.MemPerc

            NetworkIO     = $stat.NetIO

            BlockIO       = $stat.BlockIO

            Pids          = [int]$stat.PIDs

        }

    }

    return $results

}

# ----------------------------------------------------------
# Get All Docker Metrics
# ----------------------------------------------------------

function Get-AllDockerMetrics {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $status = Get-ContainerStatus `
                    -Deployment $Deployment

    $resources = Get-ContainerResourceUsage `
                    -Deployment $Deployment

    return [PSCustomObject]@{

        Timestamp = Get-Date

        Deployment = $Deployment

        ContainerStatus = $status

        ResourceUsage = $resources

    }

}

# ----------------------------------------------------------
# Show Docker Summary
# ----------------------------------------------------------

function Show-DockerSummary {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $metrics = Get-AllDockerMetrics `
                    -Deployment $Deployment

    Write-Host ""
    Write-Host "=============== Docker Summary ==============="
    Write-Host ""

    $metrics.ResourceUsage |
        Select-Object `
            Service,
            CPUPercent,
            MemoryUsage,
            MemoryPercent,
            NetworkIO,
            BlockIO,
            Pids |
        Format-Table -AutoSize

    Write-Host ""

}