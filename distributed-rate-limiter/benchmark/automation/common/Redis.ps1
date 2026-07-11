# ==========================================================
# Distributed Rate Limiter
# Redis Utility Functions
# ==========================================================

function Get-RedisServiceName {

    return "redis"

}

function Invoke-RedisCommand {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [string[]]$Command

    )

    $config = Get-DeploymentConfiguration $Deployment

    $arguments = @(
        "compose"
        "-p"
        $config.ProjectName
        "-f"
        $config.ComposeFile
        "exec"
        "-T"
        (Get-RedisServiceName)
        "redis-cli"
    )

    $arguments += $Command

    $output = & docker @arguments

    if($LASTEXITCODE -ne 0){

        throw "Redis command failed."

    }

    return $output

}

function Clear-Redis {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    Write-Host ""
    Write-Host "Flushing Redis..."

    Invoke-RedisCommand `
        -Deployment $Deployment `
        -Command @("FLUSHALL") | Out-Null

    Write-Host "Redis flushed."
    Write-Host ""

}

function Get-RedisInfo {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $raw = Invoke-RedisCommand `
                -Deployment $Deployment `
                -Command @("INFO")

    $info = @{}

    foreach($line in $raw){

        if([string]::IsNullOrWhiteSpace($line)){
            continue
        }

        if($line.StartsWith("#")){
            continue
        }

        if(!$line.Contains(":")){
            continue
        }

        $parts = $line.Split(":",2)

        $info[$parts[0]] = $parts[1]

    }

    return [PSCustomObject]$info

}

function Get-RedisMemoryInfo {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $raw = Invoke-RedisCommand `
                -Deployment $Deployment `
                -Command @("INFO","MEMORY")

    $memory = @{}

    foreach($line in $raw){

        if([string]::IsNullOrWhiteSpace($line)){
            continue
        }

        if($line.StartsWith("#")){
            continue
        }

        if(!$line.Contains(":")){
            continue
        }

        $parts = $line.Split(":",2)

        $memory[$parts[0]] = $parts[1]

    }

    return [PSCustomObject]$memory

}

function Get-RedisStats {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $raw = Invoke-RedisCommand `
                -Deployment $Deployment `
                -Command @("INFO","STATS")

    $stats = @{}

    foreach($line in $raw){

        if([string]::IsNullOrWhiteSpace($line)){
            continue
        }

        if($line.StartsWith("#")){
            continue
        }

        if(!$line.Contains(":")){
            continue
        }

        $parts = $line.Split(":",2)

        $stats[$parts[0]] = $parts[1]

    }

    return [PSCustomObject]$stats

}

function Test-RedisConnection {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $result = Invoke-RedisCommand `
                -Deployment $Deployment `
                -Command @("PING")

    return ($result -eq "PONG")

}

function Show-RedisSummary {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $memory = Get-RedisMemoryInfo $Deployment

    $stats = Get-RedisStats $Deployment

    Write-Host ""
    Write-Host "================ Redis Summary ================"
    Write-Host ""

    Write-Host ("Used Memory       : {0}" -f $memory.used_memory_human)
    Write-Host ("Peak Memory       : {0}" -f $memory.used_memory_peak_human)
    Write-Host ("Connections       : {0}" -f $stats.total_connections_received)
    Write-Host ("Commands Processed: {0}" -f $stats.total_commands_processed)
    Write-Host ("Keyspace Hits     : {0}" -f $stats.keyspace_hits)
    Write-Host ("Keyspace Misses   : {0}" -f $stats.keyspace_misses)

    Write-Host ""
    Write-Host "=============================================="
    Write-Host ""

}