param(
    [string]$Algorithm = "TOKEN_BUCKET"
)

$limit = 100

$vusList = @(
    5,
    50,
    100
)

Write-Host ""
Write-Host "==========================================="
Write-Host "Behavioral Benchmark"
Write-Host "==========================================="
Write-Host "Algorithm : $Algorithm"
Write-Host "Limit     : $limit"
Write-Host ""

foreach($vu in $vusList){

    .\benchmark\automation\run-behavioral.ps1 `
        -Algorithm $Algorithm `
        -Limit $limit `
        -VUs $vu `
        -Duration "30s"

}