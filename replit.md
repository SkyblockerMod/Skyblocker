# Skyblocker - Hypixel Skyblock Minecraft Mod

## Project Overview
Skyblocker is a Fabric mod for Minecraft 1.21.11 that adds many quality-of-life features for Hypixel Skyblock players. This is a pure Java build project — there is no web frontend or backend server.

## Tech Stack
- **Language**: Java 21
- **Build System**: Gradle 9.2.1 (with Gradle Wrapper `./gradlew`)
- **Mod Loader**: Fabric (with Fabric Loom)
- **Minecraft Version**: 1.21.11
- **Fabric Loader**: 0.18.4
- **Fabric API**: 0.140.0+1.21.11

## Key Dependencies
- YACL (Yet Another Config Lib) for configuration UI
- Mod Menu integration
- REI / EMI / JEI recipe viewer support
- NEU RepoParser for item data
- JGit for pulling data from NEU item repo
- Apache Commons Math & Text
- MoulConfig (via Dandelion)
- Discord IPC

## Java Environment
The project requires **Java 21**. The Replit environment provides Java 19 by default, but Java 21 is available in the Nix store:

```
JAVA_HOME=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6
```

Always set `JAVA_HOME` and update `PATH` before running Gradle.

## Build Instructions
```bash
export JAVA_HOME=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6
export PATH=$JAVA_HOME/bin:$PATH
./gradlew build -x test
```

The first build will download Minecraft and all dependencies (~several GB) — this takes significant time.

## Output
Built mod JAR is located at: `build/libs/skyblocker-<version>+<mc_version>.jar`

## Workflow
The **"Build Mod"** workflow runs the Gradle build using Java 21. It's a console-type workflow (not a web app).

## Recent Changes — Status Bars Overhaul

All changes are in `src/main/java/de/hysky/skyblocker/skyblock/fancybars/`.

### 1. Bug Fix — Bars Permanently Disappearing
**File**: `StatusBarsConfigScreen.java`  
**Root cause**: When a bar was dropped in free-floating mode (`currentInsertLocation == BarLocation.NULL`), `cursorBar.anchor` was never cleared. On the next `updatePositions()` call the bar remained in an invalid grid slot and was never rendered.  
**Fix**: Added `cursorBar.anchor = null;` before setting the free-float x/y coordinates in `mouseReleased()`.

### 2. New Interaction Model — Click to Select / Hold+Drag to Move
**File**: `StatusBarsConfigScreen.java`  
- Clicking a bar now **selects** it and immediately shows the `EditBarWidget` panel (previously right-click only).
- A drag only begins if the mouse moves more than `DRAG_THRESHOLD = 5px` while held down.
- New fields: `selectedBar`, `mouseButtonHeld`, `dragStartX`, `dragStartY`.
- New helper: `startDrag(StatusBar)` — extracts the old drag-init logic from `onBarClick()`.
- Clicking on empty space deselects the bar and hides the edit panel.

### 3. New Bar Visual Style
**File**: `StatusBar.java`  
- `BAR_HEIGHT = 14` (was 9, implicit from nine-slice sprite).
- Removed nine-slice sprite rendering (`BAR_FILL`, `BAR_BACK` Identifiers deleted).
- `renderBar()` now draws: 1px dark border (`0xFF1A1A1A`) → dark background (`0xFF2D2D2D`) → solid color fill.
- `drawBarFill()` and `ManaStatusBar.drawBarFill()` use `context.fill()` rectangles.
- `getHeight()` returns `BAR_HEIGHT`.
- Text Y position updated: `renderY + (BAR_HEIGHT - 9) / 2` (vertically centered in bar).
- `showMax` defaults to `false` — text shows only current value.

### 4. Reset to Default Button
**File**: `FancyStatusBars.java` — new `resetToDefaults()` method restores every bar to its `StatusBarType` default anchor, gridY, and recomputed gridX.  
**File**: `StatusBarsConfigScreen.java` — button added in `init()`, top-left corner (110×14px).  
**File**: `en_us.json` — added `"skyblocker.bars.config.resetToDefault": "Reset to Default"`.

## Project Structure
- `src/main/java/` — Main mod source code
- `src/main/resources/` — Mod resources (fabric.mod.json, mixins, assets)
- `src/test/` — Unit tests
- `src/gametest/` — In-game tests
- `buildSrc/` — Custom Gradle annotation processor plugin
- `gradle.properties` — Version configuration for all dependencies
