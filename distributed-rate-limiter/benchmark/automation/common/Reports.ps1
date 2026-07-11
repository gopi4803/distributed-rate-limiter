# ==========================================================
# Distributed Rate Limiter
# Report Utilities
# ==========================================================

# ----------------------------------------------------------
# Ensure Directory Exists
# ----------------------------------------------------------

function Ensure-Directory {

    param(

        [Parameter(Mandatory)]
        [string]$Path

    )

    if (-not (Test-Path $Path)) {

        New-Item `
            -ItemType Directory `
            -Path $Path `
            -Force | Out-Null

    }

}

# ----------------------------------------------------------
# Save JSON Report
# ----------------------------------------------------------

function Save-JsonReport {

    param(

        [Parameter(Mandatory)]
        $Data,

        [Parameter(Mandatory)]
        [string]$OutputFile

    )

    Ensure-Directory (Split-Path $OutputFile)

    $Data |
        ConvertTo-Json `
            -Depth 20 |
        Set-Content `
            -Path $OutputFile `
            -Encoding UTF8

}

# ----------------------------------------------------------
# Save CSV Report
# ----------------------------------------------------------

function Save-CsvReport {

    param(

        [Parameter(Mandatory)]
        $Data,

        [Parameter(Mandatory)]
        [string]$OutputFile

    )

    Ensure-Directory (Split-Path $OutputFile)

    $Data |
        Export-Csv `
            -Path $OutputFile `
            -NoTypeInformation `
            -Encoding UTF8

}

# ----------------------------------------------------------
# Save Markdown Report
# ----------------------------------------------------------

function Save-MarkdownReport {

    param(

        [Parameter(Mandatory)]
        [string]$Content,

        [Parameter(Mandatory)]
        [string]$OutputFile

    )

    Ensure-Directory (Split-Path $OutputFile)

    Set-Content `
        -Path $OutputFile `
        -Value $Content `
        -Encoding UTF8

}

# ----------------------------------------------------------
# Save Text Report
# ----------------------------------------------------------

function Save-TextReport {

    param(

        [Parameter(Mandatory)]
        [string]$Content,

        [Parameter(Mandatory)]
        [string]$OutputFile

    )

    Ensure-Directory (Split-Path $OutputFile)

    Set-Content `
        -Path $OutputFile `
        -Value $Content `
        -Encoding UTF8

}

# ----------------------------------------------------------
# Save Benchmark Summary
# ----------------------------------------------------------

function Save-BenchmarkSummary {

    param(

        [Parameter(Mandatory)]
        $Summary,

        [Parameter(Mandatory)]
        [string]$OutputFile

    )

    Save-JsonReport `
        -Data $Summary `
        -OutputFile $OutputFile

}

# ----------------------------------------------------------
# Load JSON Report
# ----------------------------------------------------------

function Read-JsonReport {

    param(

        [Parameter(Mandatory)]
        [string]$InputFile

    )

    if (!(Test-Path $InputFile)) {

        throw "Report not found: $InputFile"

    }

    Get-Content `
        -Raw `
        -Path $InputFile |
        ConvertFrom-Json

}