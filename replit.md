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

## Project Structure
- `src/main/java/` — Main mod source code
- `src/main/resources/` — Mod resources (fabric.mod.json, mixins, assets)
- `src/test/` — Unit tests
- `src/gametest/` — In-game tests
- `buildSrc/` — Custom Gradle annotation processor plugin
- `gradle.properties` — Version configuration for all dependencies
