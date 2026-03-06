@echo off
REM Load developer config if present (copy compile.local.example.bat to compile.local.bat and set GAME_DIR)
if exist "%~dp0compile.local.bat" call "%~dp0compile.local.bat"
if not defined GAME_DIR set GAME_DIR=YOUR_STARSECTOR_PATH

set CORE=%GAME_DIR%\starsector-core
if not exist "%CORE%\starfarer.api.jar" (
    echo Error: Starsector path not found.
    echo Copy FactionRelationships\compile.local.example.bat to compile.local.bat and set GAME_DIR to your Starsector install directory.
    exit /b 1
)

REM Read version from mod_info.json for package and zip names
for /f "delims=" %%v in ('powershell -NoProfile -Command "(Get-Content '%~dp0mod_info.json' -Raw | ConvertFrom-Json).version"') do set VERSION=%%v
set PKG=FactionRelationships-%VERSION%

set SRC=src\com\factionrelationships
set OUT=..\%PKG%\classes
set JAR=..\%PKG%\jars\FactionRelationships.jar

if not exist "%OUT%" mkdir "%OUT%"
if not exist "..\%PKG%\jars" mkdir "..\%PKG%\jars"

echo Compiling...
javac -encoding UTF-8 -cp "%CORE%\starfarer.api.jar;%CORE%\starfarer_obf.jar;%CORE%\json.jar;%CORE%\log4j-1.2.9.jar;%CORE%\lwjgl.jar;%CORE%\lwjgl_util.jar" -d "%OUT%" "%SRC%\FactionRelationshipsPlugin.java" "%SRC%\FactionRelationshipsUIRenderer.java"
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

echo Creating JAR...
jar cvf "%JAR%" -C "%OUT%" .
if errorlevel 1 (
    echo JAR creation failed.
    exit /b 1
)

echo Copying mod_info.json and config to package...
copy /Y "mod_info.json" "..\%PKG%\mod_info.json" >nul
if exist "config" xcopy /E /I /Y "config" "..\%PKG%\config" >nul

echo Creating %PKG%.zip...
powershell -NoProfile -Command "Compress-Archive -Path '..\%PKG%' -DestinationPath '..\%PKG%.zip' -Force"

echo Done. Install by copying %PKG% to Starsector\mods\FactionRelationships\ (or extract %PKG%.zip into mods)
