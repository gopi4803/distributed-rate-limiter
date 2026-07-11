# ==========================================================
# Distributed Rate Limiter
# Spring Boot Metrics Utilities
# ==========================================================

# ----------------------------------------------------------
# Micrometer Metric Names
# ----------------------------------------------------------

$SpringMetricNames = @{
    AllowedRequests          = "ratelimiter.requests.allowed"
    BlockedRequests          = "ratelimiter.requests.blocked"
    RedisFailures            = "ratelimiter.redis.failures"
    RequestDuration          = "ratelimiter.request.duration"
    CircuitBreakerTransitions = "ratelimiter.circuitbreaker.open.transitions"
}

# ----------------------------------------------------------
# Get Metric
# ----------------------------------------------------------

function Get-Metric {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode", "Distributed")]
        [string]$Deployment,

        [Parameter(Mandatory)]
        [string]$MetricName,

        [Parameter(Mandatory)]
        [string]$DisplayName

    )

    $config = Get-DeploymentConfiguration -Deployment $Deployment

    $results = @()

    foreach ($instance in $config.Instances) {

        $metricUrl = $instance.Url +
                     $config.MetricsEndpoint +
                     "/" +
                     $MetricName

        try {

            $response = Invoke-RestMethod `
                            -Uri $metricUrl `
                            -Method GET `
                            -TimeoutSec 10

            foreach ($measurement in $response.measurements) {

                $results += [PSCustomObject]@{

                    Timestamp  = Get-Date

                    Deployment = $Deployment

                    Instance   = $instance.Name

                    Metric     = $DisplayName

                    Statistic  = $measurement.statistic

                    Value      = $measurement.value

                    BaseUnit   = $response.baseUnit

                }

            }

        }
        catch {

            Write-Warning (
                "Unable to retrieve metric '{0}' from '{1}'. {2}" -f `
                $MetricName,
                $instance.Name,
                $_.Exception.Message
            )

        }

    }

    return $results

}

# ----------------------------------------------------------
# Allowed Requests
# ----------------------------------------------------------

function Get-AllowedRequests {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    return Get-Metric `
                -Deployment $Deployment `
                -MetricName $SpringMetricNames.AllowedRequests `
                -DisplayName "AllowedRequests"

}

# ----------------------------------------------------------
# Blocked Requests
# ----------------------------------------------------------

function Get-BlockedRequests {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    return Get-Metric `
                -Deployment $Deployment `
                -MetricName $SpringMetricNames.BlockedRequests `
                -DisplayName "BlockedRequests"

}

# ----------------------------------------------------------
# Redis Failures
# ----------------------------------------------------------

function Get-RedisFailures {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    return Get-Metric `
                -Deployment $Deployment `
                -MetricName $SpringMetricNames.RedisFailures `
                -DisplayName "RedisFailures"

}

# ----------------------------------------------------------
# Request Duration
# ----------------------------------------------------------

function Get-RequestDuration {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    return Get-Metric `
                -Deployment $Deployment `
                -MetricName $SpringMetricNames.RequestDuration `
                -DisplayName "RequestDuration"

}

# ----------------------------------------------------------
# Circuit Breaker Transitions
# ----------------------------------------------------------

function Get-CircuitBreakerTransitions {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    return Get-Metric `
                -Deployment $Deployment `
                -MetricName $SpringMetricNames.CircuitBreakerTransitions `
                -DisplayName "CircuitBreakerTransitions"

}

# ----------------------------------------------------------
# Collect All Spring Metrics
# ----------------------------------------------------------

function Get-AllSpringMetrics {

    param(

        [Parameter(Mandatory)]
        [ValidateSet("SingleNode","Distributed")]
        [string]$Deployment

    )

    $results = @()

    $results += Get-AllowedRequests `
                    -Deployment $Deployment

    $results += Get-BlockedRequests `
                    -Deployment $Deployment

    $results += Get-RedisFailures `
                    -Deployment $Deployment

    $results += Get-RequestDuration `
                    -Deployment $Deployment

    $results += Get-CircuitBreakerTransitions `
                    -Deployment $Deployment

    return $results

}