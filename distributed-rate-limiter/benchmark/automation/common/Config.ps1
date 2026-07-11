# ==========================================================
# Distributed Rate Limiter
# Benchmark Common Configuration
# ==========================================================

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "../../..")

$DockerDirectory = Join-Path $ProjectRoot "docker"

$BenchmarkDirectory = Join-Path $ProjectRoot "benchmark"

$EnvironmentFile = Join-Path $DockerDirectory "benchmark.env"

$GraphScript = Join-Path $BenchmarkDirectory "scripts/generate_graphs.py"

$BehavioralGraphScript = Join-Path $BenchmarkDirectory "scripts/generate_behavioral_graphs.py"

function Get-ProjectRoot {
    return $ProjectRoot
}

function Get-DockerDirectory {
    return $DockerDirectory
}

function Get-BenchmarkDirectory {
    return $BenchmarkDirectory
}

function Get-EnvironmentFile {
    return $EnvironmentFile
}

function Get-GraphScript {
    return $GraphScript
}

function Get-BehavioralGraphScript {
    return $BehavioralGraphScript
}

function Get-DeploymentConfiguration {

    param(
        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment
    )

    #
    # Clone the configuration so we don't modify the original
    #

    $configuration = @{}

    foreach ($key in $BenchmarkDeployments[$Deployment].Keys) {
        $configuration[$key] = $BenchmarkDeployments[$Deployment][$key]
    }

    #
    # Convert project-relative paths into absolute paths
    #

    $projectRoot = Get-ProjectRoot

    $configuration.ComposeFile = Join-Path `
        $projectRoot `
        $configuration.ComposeFile

    $configuration.ResultDirectory = Join-Path `
        $projectRoot `
        $configuration.ResultDirectory

    $configuration.ReportDirectory = Join-Path `
        $projectRoot `
        $configuration.ReportDirectory

    $configuration.GraphDirectory = Join-Path `
        $projectRoot `
        $configuration.GraphDirectory

    return $configuration

}