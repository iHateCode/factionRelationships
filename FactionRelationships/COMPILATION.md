# Compilation Reference Guide

## Project Overview

**Faction Relationships** – A Starsector mod that shows a list of factions and their relationship with the player on the main navigation screen.

- **Mod ID**: `factionrelationships`
- **Game Version**: `0.98a-RC8`
- **Mod Plugin**: `com.factionrelationships.FactionRelationshipsPlugin`

## Prerequisites

1. **JDK 17** – Starsector 0.98a uses Java 17. Install JDK 17 and run `javac -version`.
2. **Starsector** – JARs are taken from your game install (the folder that contains `starsector-core`).

## Developer config (optional)

To avoid editing `compile.bat` and to keep your local paths out of the repo:

1. Copy `FactionRelationships/compile.local.example.bat` to `FactionRelationships/compile.local.bat`.
2. Edit `compile.local.bat` and set `GAME_DIR` to your Starsector install directory.

`compile.local.bat` is gitignored; only the example file is committed. The main `compile.bat` loads it automatically when present.

## Project Structure

```
FactionRelationships/
├── mod_info.json
├── config/
│   └── faction_relationships_config.json   # optional: maxFactions (default 15, clamped 1–50)
├── src/
│   └── com/factionrelationships/
│       ├── FactionRelationshipsPlugin.java
│       └── FactionRelationshipsUIRenderer.java
├── compile.local.example.bat   # copy to compile.local.bat (gitignored) to set GAME_DIR
└── COMPILATION.md
```

Output: `FactionRelationships-<version>/` (folder and zip), where `<version>` comes from `mod_info.json` (e.g. `FactionRelationships-1.0.0/`, `FactionRelationships-1.0.0.zip`). Contains `classes/`, `jars/FactionRelationships.jar`, and `config/`.

## Compile

From the repo root, run the build script:

```batch
FactionRelationships\compile.bat
```

Or manually:

```batch
set GAME_DIR=YOUR_STARSECTOR_PATH
set CORE=%GAME_DIR%\starsector-core
set VERSION=1.0.0
javac -encoding UTF-8 -cp "%CORE%\starfarer.api.jar;%CORE%\starfarer_obf.jar;%CORE%\log4j-1.2.9.jar;%CORE%\lwjgl.jar;%CORE%\lwjgl_util.jar" -d FactionRelationships-%VERSION%\classes FactionRelationships\src\com\factionrelationships\*.java
jar cvf FactionRelationships-%VERSION%\jars\FactionRelationships.jar -C FactionRelationships-%VERSION%\classes .
```

Set `GAME_DIR` before running (e.g. via `compile.local.bat` as above, or in the batch session).

## Package and Install

1. The build script copies `mod_info.json` and config into the versioned package folder (e.g. `FactionRelationships-1.0.0/`).
2. Copy that folder into your game `mods` folder, then rename to `FactionRelationships` for the game to load it, or use the zip:

   ```
   xcopy /E /I FactionRelationships-1.0.0 "YOUR_STARSECTOR_PATH\mods\FactionRelationships\"
   ```
   Or extract `FactionRelationships-1.0.0.zip` into `mods` and rename the extracted folder to `FactionRelationships`.

3. Final layout in your game `mods` folder:

   ```
   mods\FactionRelationships\
   ├── mod_info.json
   ├── config\
   │   └── faction_relationships_config.json
   └── jars\
       └── FactionRelationships.jar
   ```

   To change how many factions are shown, edit `config/faction_relationships_config.json` and set `maxFactions` (1–50). Default is 15.

## Quick Reference

- **Game directory**: Set `GAME_DIR` in `compile.local.bat` (recommended) or in `compile.bat` / manual commands.
- **Source**: `FactionRelationships/src/com/factionrelationships/`
- **Output**: `FactionRelationships-<version>/` and `FactionRelationships-<version>.zip` (version from `mod_info.json`); JAR at `jars/FactionRelationships.jar` inside the package.
