@echo off
REM Copy this file to compile.local.bat and set your paths.
REM compile.local.bat is gitignored and will be used by compile.bat when present.

set GAME_DIR=YOUR_STARSECTOR_PATH

REM Optional: if LunaLib/LazyLib use versioned folder names (e.g. LunaLib-2.0.5), set these so the build finds the JARs:
REM set LUNALIB=%GAME_DIR%\mods\LunaLib-2.0.5\jars\LunaLib.jar
REM set LAZYLIB=%GAME_DIR%\mods\LazyLib-3.0.0\jars\LazyLib.jar
