param(
    [string]$Algorithm = "TOKEN_BUCKET",
    [int]$Limit = 100,
    [int]$VUs = 5,
    [string]$Duration = "30s"
)

$ResultDir = "benchmark/results/behavioral/$Algorithm/limit-$Limit"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $ResultDir | Out-Null

$OutputFile = "$ResultDir/$($VUs)vu-summary.json"

Write-Host ""
Write-Host "==============================================="
Write-Host "Behavioral Benchmark"
Write-Host "==============================================="
Write-Host "Algorithm : $Algorithm"
Write-Host "Limit     : $Limit"
Write-Host "VUs       : $VUs"
Write-Host "Duration  : $Duration"
Write-Host ""

##########################################################
# Verify application
##########################################################

Write-Host "Checking application..."

curl.exe http://localhost:8080/actuator/health

##########################################################
# Flush Redis
##########################################################

Write-Host ""
Write-Host "Flushing Redis..."

docker exec drl-redis redis-cli FLUSHALL

##########################################################
# Run Benchmark
##########################################################

Write-Host ""
Write-Host "Output File:"
Write-Host $OutputFile
Write-Host ""

k6 run `
    -e BENCHMARK_LIMIT=$Limit `
    -e BENCHMARK_ALGORITHM=$Algorithm `
    -e VUS=$VUs `
    -e DURATION=$Duration `
    --summary-export "$OutputFile" `
    benchmark/scenarios/03-behavioral.js

##########################################################
# Metrics
##########################################################

Write-Host ""
Write-Host "Allowed"

curl.exe http://localhost:8080/actuator/metrics/ratelimiter.requests.allowed

Write-Host ""
Write-Host "Blocked"

curl.exe http://localhost:8080/actuator/metrics/ratelimiter.requests.blocked

Write-Host ""
Write-Host "Redis Failures"

curl.exe http://localhost:8080/actuator/metrics/ratelimiter.redis.failures

Write-Host ""
Write-Host "Circuit Breaker"

curl.exe http://localhost:8080/actuator/metrics/ratelimiter.circuitbreaker.open.transitions

Write-Host ""
Write-Host "Done."