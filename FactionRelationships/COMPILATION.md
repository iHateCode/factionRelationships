# Compilation Reference Guide

## Project Overview

**Faction Relationships** – A Starsector mod that shows a list of factions and their relationship with the player on the main navigation screen.

- **Mod ID**: `factionrelationships`
- **Game Version**: `0.98.5a-RC9`
- **Mod Plugin**: `com.factionrelationships.FactionRelationshipsPlugin`
- **Dependencies**: LazyLib and LunaLib (required at runtime and for compilation).

## Prerequisites

1. **JDK 17** – Starsector 0.98.5a uses Java 17. Install JDK 17 and run `javac -version`.
2. **Starsector** – JARs are taken from your game install (the folder that contains `starsector-core`).
3. **LunaLib and LazyLib** – Install both mods in your Starsector `mods` folder. The compile script looks for `mods/LunaLib/jars/LunaLib.jar` and `mods/LazyLib/jars/LazyLib.jar` by default. If your mod folders use versioned names (e.g. `LunaLib-2.0.5`, `LazyLib-3.0.0`), set `$LunaLib` and `$LazyLib` in `compile.local.ps1` to point at those JARs (see Developer config below). Required for compilation and at runtime.

## Developer config (optional)

To avoid editing `compile.ps1` and to keep your local paths out of the repo:

1. Copy `FactionRelationships/compile.local.example.ps1` to `FactionRelationships/compile.local.ps1`.
2. Edit `compile.local.ps1` and set `$GameDir` to your Starsector install directory.
3. If LunaLib or LazyLib use versioned folder names (e.g. `LunaLib-2.0.5`), uncomment and set `$LunaLib` and `$LazyLib` to the full paths to their JARs.

`compile.local.ps1` is gitignored; only the example file is committed. `compile.ps1` loads it automatically when present.

## Project Structure

```
FactionRelationships/
├── mod_info.json
├── FactionRelationships.version   # Version Checker metadata (optional; copied into dist)
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
├── compile.local.example.ps1   # copy to compile.local.ps1 (gitignored) to set $GameDir
└── COMPILATION.md
```

Output: written to **`dist/`** at the repo root (gitignored). The build produces `dist/FactionRelationships-<version>/` (folder) and `dist/FactionRelationships-<version>.zip`, where `<version>` comes from `mod_info.json`. Each package contains `classes/`, `jars/FactionRelationships.jar`, `config/`, and `data/`. In-game settings and keybind are configured via **Mod Settings** (F2 in campaign) under this mod's section.

## Compile

From the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File FactionRelationships\compile.ps1
```

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
   ├── FactionRelationships.version
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

- **Game directory**: Set `$GameDir` in `compile.local.ps1`. Optionally set `$LunaLib`/`$LazyLib` if you use versioned LunaLib/LazyLib folders.
- **Source**: `FactionRelationships/src/com/factionrelationships/`
- **Output**: `dist/FactionRelationships-<version>/` and `dist/FactionRelationships-<version>.zip` (version from `mod_info.json`); JAR at `jars/FactionRelationships.jar` inside the package. The `dist/` folder is gitignored.
