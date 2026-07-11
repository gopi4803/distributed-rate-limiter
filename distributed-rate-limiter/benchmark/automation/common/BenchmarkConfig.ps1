# ==========================================================
# Distributed Rate Limiter
# Benchmark Configuration Utilities
# ==========================================================

function Get-BenchmarkConfiguration {

    $envFile = Get-EnvironmentFile

    if (!(Test-Path $envFile)) {
        throw "benchmark.env not found at $envFile"
    }

    $configuration = @{}

    Get-Content $envFile | ForEach-Object {

        $line = $_.Trim()

        if ($line.Length -eq 0) {
            return
        }

        if ($line.StartsWith("#")) {
            return
        }

        $parts = $line.Split("=",2)

        if ($parts.Length -eq 2) {

            $configuration[$parts[0].Trim()] = $parts[1].Trim()

        }

    }

    return $configuration

}

function Set-BenchmarkConfiguration {

    param(

        [Parameter(Mandatory)]
        [ValidateSet(
            "TOKEN_BUCKET",
            "SLIDING_WINDOW_COUNTER",
            "FIXED_WINDOW"
        )]
        [string]$Algorithm,

        [int]$Limit = 100,

        [string]$Window = "1m"

    )

    $envFile = Get-EnvironmentFile

    if (!(Test-Path $envFile)) {
        throw "benchmark.env not found."
    }

    $lines = Get-Content $envFile

    $updated = @()

    foreach($line in $lines) {

        if($line.StartsWith("BENCHMARK_ALGORITHM=")) {

            $updated += "BENCHMARK_ALGORITHM=$Algorithm"

        }
        elseif($line.StartsWith("BENCHMARK_LIMIT=")) {

            $updated += "BENCHMARK_LIMIT=$Limit"

        }
        elseif($line.StartsWith("BENCHMARK_WINDOW=")) {

            $updated += "BENCHMARK_WINDOW=$Window"

        }
        else {

            $updated += $line

        }

    }

    Set-Content `
        -Path $envFile `
        -Value $updated

}

function Show-BenchmarkConfiguration {

    $configuration = Get-BenchmarkConfiguration

    Write-Host ""
    Write-Host "====================================="
    Write-Host "Current Benchmark Configuration"
    Write-Host "====================================="
    Write-Host ""

    $configuration.GetEnumerator() |
        Sort-Object Name |
        Format-Table -AutoSize

}

function Reset-BenchmarkConfiguration {

    Set-BenchmarkConfiguration `
        -Algorithm TOKEN_BUCKET `
        -Limit 100 `
        -Window "1m"

}