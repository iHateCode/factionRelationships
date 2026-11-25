#Requires -Version 5.1
<#
.SYNOPSIS
    Extracts mod-related lines and error lines from Starsector's starsector.log.
.DESCRIPTION
    Uses $GameDir from compile.local.ps1 (same convention as compile.ps1), then reads:
    $GameDir\starsector-core\starsector.log

    Writes a timestamped output file next to this script.
#>

param(
    [string]$LogPath,
    [string]$OutputPath
)

# Load local developer config (same as compile.ps1).
$localConfig = Join-Path $PSScriptRoot "compile.local.ps1"
if (Test-Path $localConfig) { . $localConfig }

if (-not $LogPath) {
    if (-not $GameDir) {
        Write-Error "GameDir is not set. Copy compile.local.example.ps1 to compile.local.ps1 and set `$GameDir."
        exit 1
    }
    $LogPath = Join-Path $GameDir "starsector-core\starsector.log"
}

if (-not (Test-Path $LogPath)) {
    Write-Error "Log file not found: $LogPath"
    exit 1
}

if (-not $OutputPath) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $OutputPath = Join-Path $PSScriptRoot "factionrelationships-log-extract-$timestamp.log"
}

$modRegex = '(?i)(factionrelationships|faction relationships|com\.factionrelationships|factionrelationshipsplugin)'
$errorRegex = '(?i)(\berror\b|\bexception\b|\bfatal\b|\bsevere\b|^\s*caused by:|^\s*at\s+\S+\(.*\)$)'

$lines = Get-Content -Path $LogPath -ErrorAction Stop

$modMatches = New-Object System.Collections.Generic.List[string]
$errorMatches = New-Object System.Collections.Generic.List[string]

for ($i = 0; $i -lt $lines.Count; $i++) {
    $lineNumber = $i + 1
    $line = $lines[$i]
    $tagged = "[$lineNumber] $line"

    if ($line -match $modRegex) {
        $modMatches.Add($tagged)
    }

    if ($line -match $errorRegex) {
        $errorMatches.Add($tagged)
    }
}

# De-duplicate while preserving first occurrence order.
$seen = @{}
$modUnique = foreach ($item in $modMatches) {
    if (-not $seen.ContainsKey($item)) {
        $seen[$item] = $true
        $item
    }
}

$seen = @{}
$errorUnique = foreach ($item in $errorMatches) {
    if (-not $seen.ContainsKey($item)) {
        $seen[$item] = $true
        $item
    }
}

$report = New-Object System.Collections.Generic.List[string]
$report.Add("Source log: $LogPath")
$report.Add("Generated:  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$report.Add("")
$report.Add("=== MOD-RELATED LINES ($($modUnique.Count)) ===")
if ($modUnique.Count -gt 0) {
    $modUnique | ForEach-Object { [void]$report.Add($_) }
} else {
    $report.Add("(none)")
}

$report.Add("")
$report.Add("=== ERROR LINES ($($errorUnique.Count)) ===")
if ($errorUnique.Count -gt 0) {
    $errorUnique | ForEach-Object { [void]$report.Add($_) }
} else {
    $report.Add("(none)")
}

$report | Set-Content -Path $OutputPath -Encoding UTF8

Write-Host "Wrote extracted log to: $OutputPath"
Write-Host "Mod-related lines: $($modUnique.Count)"
Write-Host "Error lines: $($errorUnique.Count)"
