# Faction Relationships

A [Starsector](https://fractalsoftworks.com/) mod that shows a list of factions and their relationship with the player on the main navigation screen.

![Faction relationships in game](assets/screenshot.png)

- **Game version**: 0.98a-RC8  
- **Mod ID**: `factionrelationships`
- **Dependencies**: [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=12771.0) and [LunaLib](https://github.com/Lukas22041/LunaLib) (required; enable them in the launcher).

## Installation

1. Install **LazyLib** and **LunaLib** in your Starsector `mods` folder if you have not already.
2. Download the latest release (or build from source; see below).
3. Extract the mod folder into your Starsector `mods` directory.
4. Enable the mod (and LazyLib, LunaLib) in the game launcher.

Your `mods` folder should contain something like:

```
mods/FactionRelationships/
├── mod_info.json
├── config/
│   └── faction_relationships_config.json   # deprecated; settings via Mod Settings
├── data/
│   └── config/
│       └── LunaSettings.csv
└── jars/
    └── FactionRelationships.jar
```

## Configuration

Configure the mod in-game via **Mod Settings** (press **F2** in campaign): max factions shown, text size, overlay keybind (toggle or hold-to-view), and optional “show only hostile factions” filter, relationship-change display in overlay (duration configurable, default 30 seconds), and optional auto-show overlay when a relationship changes (when enabled, the overlay auto-hides after the same configured duration). No need to edit JSON files.

## Building from source

- **Requirements**: JDK 17, a Starsector 0.98a install (for API JARs), and **LunaLib** and **LazyLib** installed in your game `mods` folder (the build script needs their JARs).
- **Build**: From the repo root, run `FactionRelationships\compile.bat`.  
  Copy `FactionRelationships\compile.local.example.bat` to `FactionRelationships\compile.local.bat` and set `GAME_DIR` to your Starsector path (and optionally `LUNALIB` / `LAZYLIB` if you use versioned mod folders).
- **Details**: See [FactionRelationships/COMPILATION.md](FactionRelationships/COMPILATION.md) for full build steps, project layout, and manual compile commands.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file.
