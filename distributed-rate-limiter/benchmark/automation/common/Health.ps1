# ==========================================================
# Distributed Rate Limiter
# Health Utilities
# ==========================================================

function Wait-ForDeploymentHealth {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment,

        [int]$TimeoutSeconds = 120,

        [int]$PollIntervalSeconds = 2

    )

    $config = Get-DeploymentConfiguration $Deployment

    Write-Host ""
    Write-Host "Waiting for deployment health..."
    Write-Host ""

    foreach ($instance in $config.Instances) {

        $healthUrl = $instance.Url + $config.HealthEndpoint

        Write-Host "Checking $($instance.Name)..."

        $healthy = $false

        $timer = [System.Diagnostics.Stopwatch]::StartNew()

        while ($timer.Elapsed.TotalSeconds -lt $TimeoutSeconds) {

            try {

                $response = Invoke-RestMethod `
                    -Uri $healthUrl `
                    -Method GET `
                    -TimeoutSec 5

                if ($response.status -eq "UP") {

                    Write-Host "  [OK] Healthy"

                    $healthy = $true

                    break

                }

            }
            catch {

                # Ignore while waiting

            }

            Start-Sleep -Seconds $PollIntervalSeconds

        }

        if (-not $healthy) {

            throw "$($instance.Name) failed health check."

        }

    }

    Write-Host ""
    Write-Host "Deployment is healthy."
    Write-Host ""

}