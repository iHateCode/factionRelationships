#Requires -Version 5.1
<#
.SYNOPSIS
    Builds FactionRelationships.jar and packages the mod.
.DESCRIPTION
    Copy compile.local.example.ps1 to compile.local.ps1 and set $GameDir
    (and optionally $LunaLib / $LazyLib) for your machine. That file is
    gitignored. This script loads it automatically when present.
#>

# --- Load developer config -----------------------------------------------------------
$localConfig = Join-Path $PSScriptRoot "compile.local.ps1"
if (Test-Path $localConfig) { . $localConfig }

if (-not $GameDir) {
    Write-Error "GameDir is not set. Copy compile.local.example.ps1 to compile.local.ps1 and set `$GameDir."
    exit 1
}

if (-not $LunaLib) { $LunaLib = Join-Path $GameDir "mods\LunaLib\jars\LunaLib.jar" }
if (-not $LazyLib)  { $LazyLib  = Join-Path $GameDir "mods\LazyLib\jars\LazyLib.jar" }

$Core = Join-Path $GameDir "starsector-core"

# --- Validate prerequisites ----------------------------------------------------------
if (-not (Test-Path (Join-Path $Core "starfarer.api.jar"))) {
    Write-Error "Starsector not found at: $GameDir"
    exit 1
}
if (-not (Test-Path $LunaLib)) {
    Write-Error "LunaLib not found at: $LunaLib"
    exit 1
}
if (-not (Test-Path $LazyLib)) {
    Write-Error "LazyLib not found at: $LazyLib"
    exit 1
}

# --- Version / paths -----------------------------------------------------------------
$version = (Get-Content (Join-Path $PSScriptRoot "mod_info.json") -Raw | ConvertFrom-Json).version
if ([string]::IsNullOrWhiteSpace($version)) {
    Write-Error "Could not read 'version' from mod_info.json; refusing to build."
    exit 1
}
$pkg     = "FactionRelationships-$version"
$dist    = Join-Path $PSScriptRoot "..\dist"
$pkgDir  = Join-Path $dist $pkg
# Compiler intermediates live outside $pkgDir so they are not shipped in the mod.
$out     = Join-Path $dist "build\classes"
$jarDir  = Join-Path $pkgDir "jars"
$jar     = Join-Path $jarDir "FactionRelationships.jar"
$src     = Join-Path $PSScriptRoot "src\com\factionrelationships"

# --- Create output dirs --------------------------------------------------------------
# Start both from empty: a surviving $pkgDir makes Copy-Item nest directories inside
# themselves (config\config\), and stale .class files would be packed into the JAR
# even after their source file was deleted.
Remove-Item -Recurse -Force $pkgDir -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force $out    -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $out    | Out-Null
New-Item -ItemType Directory -Force -Path $jarDir | Out-Null

# --- Compile -------------------------------------------------------------------------
$cp = @(
    "$Core\starfarer.api.jar"
    "$Core\starfarer_obf.jar"
    "$Core\json.jar"
    "$Core\log4j-1.2.9.jar"
    "$Core\lwjgl.jar"
    "$Core\lwjgl_util.jar"
    $LazyLib
    $LunaLib
) -join ";"

$sources = Get-ChildItem -Path $src -Filter "*.java" | Select-Object -ExpandProperty FullName

Write-Host "Compiling..."
& javac -encoding UTF-8 -cp $cp -d $out @sources
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed."; exit 1 }

# --- Package JAR ---------------------------------------------------------------------
Write-Host "Creating JAR..."
& jar cvf $jar -C $out .
if ($LASTEXITCODE -ne 0) { Write-Error "JAR creation failed."; exit 1 }

# --- Copy mod files ------------------------------------------------------------------
Write-Host "Copying mod files..."
Copy-Item (Join-Path $PSScriptRoot "mod_info.json") $pkgDir -Force

$versionFile = Join-Path $PSScriptRoot "FactionRelationships.version"
if (Test-Path $versionFile) { Copy-Item $versionFile $pkgDir -Force }

foreach ($folder in @("data")) {
    $src2 = Join-Path $PSScriptRoot $folder
    if (Test-Path $src2) {
        # Destination is $pkgDir, not $pkgDir\$folder: Copy-Item nests a directory
        # inside an existing destination of the same name.
        Copy-Item $src2 $pkgDir -Recurse -Force
    }
}

# --- Zip -----------------------------------------------------------------------------
Write-Host "Creating $pkg.zip..."
Compress-Archive -Path $pkgDir -DestinationPath (Join-Path $dist "$pkg.zip") -Force

Write-Host "Creating FactionRelationships.zip..."
Compress-Archive -Path $pkgDir -DestinationPath (Join-Path $dist "FactionRelationships.zip") -Force

Write-Host "Done. Output in dist\$pkg\ and dist\$pkg.zip."
