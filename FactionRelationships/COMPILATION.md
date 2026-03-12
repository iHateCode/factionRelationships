# Compilation Reference Guide

## Project Overview

**Faction Relationships** – A Starsector mod that shows a list of factions and their relationship with the player on the main navigation screen.

- **Mod ID**: `factionrelationships`
- **Game Version**: `0.98a-RC8`
- **Mod Plugin**: `com.factionrelationships.FactionRelationshipsPlugin`
- **Dependencies**: LazyLib and LunaLib (required at runtime and for compilation).

## Prerequisites

1. **JDK 17** – Starsector 0.98a uses Java 17. Install JDK 17 and run `javac -version`.
2. **Starsector** – JARs are taken from your game install (the folder that contains `starsector-core`).
3. **LunaLib and LazyLib** – Install both mods in your Starsector `mods` folder. The compile script looks for `mods/LunaLib/jars/LunaLib.jar` and `mods/LazyLib/jars/LazyLib.jar` by default. If your mod folders use versioned names (e.g. `LunaLib-2.0.5`, `LazyLib-3.0.0`), set `LUNALIB` and `LAZYLIB` in `compile.local.bat` to point at those JARs (see Developer config below). Required for compilation and at runtime.

## Developer config (optional)

To avoid editing `compile.bat` and to keep your local paths out of the repo:

1. Copy `FactionRelationships/compile.local.example.bat` to `FactionRelationships/compile.local.bat`.
2. Edit `compile.local.bat` and set `GAME_DIR` to your Starsector install directory.
3. If LunaLib or LazyLib use versioned folder names (e.g. `LunaLib-2.0.5`), set `LUNALIB` and `LAZYLIB` to the full paths to their JARs so the build can find them.

`compile.local.bat` is gitignored; only the example file is committed. The main `compile.bat` loads it automatically when present.

## Project Structure

```
FactionRelationships/
├── mod_info.json
├── config/
│   └── faction_relationships_config.json   # deprecated; settings via LunaLib (see data/config)
├── data/
│   └── config/
│       └── LunaSettings.csv   # LunaLib settings (max factions, text size, overlay keybind)
├── src/
│   └── com/factionrelationships/
│       ├── FactionRelationshipsPlugin.java
│       ├── FactionRelationshipsUIRenderer.java
│       ├── FactionRelationshipsCampaignInputListener.java
│       ├── FactionRelationshipsKeybindScript.java   # stub for save compatibility
│       ├── FactionRelationshipChangeListener.java
│       ├── RelationshipChangeStore.java
│       └── SystemFactionRelationshipsIntel.java
├── compile.local.example.bat   # copy to compile.local.bat (gitignored) to set GAME_DIR
└── COMPILATION.md
```

Output: written to **`dist/`** at the repo root (gitignored). The build produces `dist/FactionRelationships-<version>/` (folder) and `dist/FactionRelationships-<version>.zip`, where `<version>` comes from `mod_info.json`. Each package contains `classes/`, `jars/FactionRelationships.jar`, `config/`, and `data/`. In-game settings and keybind are configured via **Mod Settings** (F2 in campaign) under this mod's section.

## Compile

From the repo root, run the build script:

```batch
FactionRelationships\compile.bat
```

Or manually (set `LUNALIB` and `LAZYLIB` if using versioned mod folders):

```batch
set GAME_DIR=YOUR_STARSECTOR_PATH
set LUNALIB=%GAME_DIR%\mods\LunaLib\jars\LunaLib.jar
set LAZYLIB=%GAME_DIR%\mods\LazyLib\jars\LazyLib.jar
set CORE=%GAME_DIR%\starsector-core
set VERSION=1.3.0
javac -encoding UTF-8 -cp "%CORE%\starfarer.api.jar;%CORE%\starfarer_obf.jar;%CORE%\json.jar;%CORE%\log4j-1.2.9.jar;%CORE%\lwjgl.jar;%CORE%\lwjgl_util.jar;%LAZYLIB%;%LUNALIB%" -d FactionRelationships-%VERSION%\classes FactionRelationships\src\com\factionrelationships\FactionRelationshipsPlugin.java FactionRelationships\src\com\factionrelationships\FactionRelationshipsUIRenderer.java FactionRelationships\src\com\factionrelationships\FactionRelationshipsCampaignInputListener.java FactionRelationships\src\com\factionrelationships\FactionRelationshipsKeybindScript.java FactionRelationships\src\com\factionrelationships\FactionRelationshipChangeListener.java FactionRelationships\src\com\factionrelationships\RelationshipChangeStore.java FactionRelationships\src\com\factionrelationships\SystemFactionRelationshipsIntel.java
jar cvf FactionRelationships-%VERSION%\jars\FactionRelationships.jar -C FactionRelationships-%VERSION%\classes .
```

Set `GAME_DIR` (and optionally `LUNALIB`, `LAZYLIB`) before running (e.g. via `compile.local.bat` as above, or in the batch session).

## Package and Install

1. The build script copies `mod_info.json` and config into the versioned package folder under `dist/` (e.g. `dist/FactionRelationships-1.0.0/`).
2. Copy that folder into your game `mods` folder, then rename to `FactionRelationships` for the game to load it, or use the zip:

   ```
   xcopy /E /I dist\FactionRelationships-1.0.0 "YOUR_STARSECTOR_PATH\mods\FactionRelationships\"
   ```
   Or extract `dist/FactionRelationships-1.0.0.zip` into `mods` and rename the extracted folder to `FactionRelationships`.

3. Final layout in your game `mods` folder:

   ```
   mods\FactionRelationships\
   ├── mod_info.json
   ├── config\
   │   └── faction_relationships_config.json
   ├── data\
   │   └── config\
   │       └── LunaSettings.csv
   └── jars\
       └── FactionRelationships.jar
   ```

   Configure max factions, text size, overlay keybind (toggle or hold), hostile-only filter, show-only-factions-in-current-system (and hyperspace fallback), relationship-change display, auto-show overlay on change, and optional auto-hide overlay after N seconds in **Mod Settings** (F2 in campaign).

## Quick Reference

- **Game directory**: Set `GAME_DIR` in `compile.local.bat` (recommended) or in `compile.bat` / manual commands. Optionally set `LUNALIB` and `LAZYLIB` in `compile.local.bat` if you use versioned LunaLib/LazyLib folders.
- **Source**: `FactionRelationships/src/com/factionrelationships/`
- **Output**: `dist/FactionRelationships-<version>/` and `dist/FactionRelationships-<version>.zip` (version from `mod_info.json`); JAR at `jars/FactionRelationships.jar` inside the package. The `dist/` folder is gitignored.
