# ==========================================================
# Distributed Rate Limiter
# Report Generator
# ==========================================================

function New-BenchmarkReport {

    param(

        [Parameter(Mandatory)]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [string]$Algorithm,

        [Parameter(Mandatory)]
        [int]$Limit,

        [Parameter(Mandatory)]
        [string]$Window

    )

    $config = Get-DeploymentConfiguration `
                    -Deployment $Deployment

    Ensure-Directory `
        -Path $config.ReportDirectory

    $reportFile = Join-Path `
                    $config.ReportDirectory `
                    "benchmark-report.md"

    $content = @"

# Distributed Rate Limiter Benchmark Report

Generated: $(Get-Date)

---

## Deployment

- Deployment : $Deployment
- Algorithm  : $Algorithm
- Limit      : $Limit
- Window     : $Window

---

## Validation

See:

benchmark/results/$($Deployment.ToLower())/validation

---

## Infrastructure

See:

benchmark/results/$($Deployment.ToLower())/infrastructure

---

## Behavioral

See:

benchmark/results/$($Deployment.ToLower())/behavioral

---

## Docker Metrics

Captured automatically.

---

## Redis Metrics

Captured automatically.

---

## Spring Metrics

Captured automatically.

"@

    Set-Content `
        -Path $reportFile `
        -Value $content

    Write-Host ""
    Write-Host "Benchmark report generated."
    Write-Host $reportFile
    Write-Host ""

}