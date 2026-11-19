# Averion Client

Averion Client is the Fabric-based Minecraft Java Edition client for the Averion ecosystem. It hosts Averion's own gameplay-facing client features and integrations. The separate Averion Launcher manages profiles, accounts, downloads and launch.

Phase 01 adds an in-game HUD that shows Averion Client branding and the active Minecraft version. Press **F8** while playing to toggle it. A short notice confirms the change and the setting persists across restarts. To rebind it, find **Toggle Averion HUD** under **Averion Client** in Minecraft's Controls or Key Binds screen. [Configuration](docs/CONFIGURATION.md), [Feature system](docs/FEATURE_SYSTEM.md).

The build targets are Minecraft **1.16.5** and **26.3**. Intermediate versions require separate ports and verification. The [Phase 01 report](docs/PHASE_01_REPORT.md) records which runtime checks were performed.

## Build

Install JDK 25 and set `JAVA_HOME` to it. Use the checked-in Gradle 9.6.0 wrapper. The first build needs network access to download Minecraft development files and build dependencies. On Windows, use `gradlew.bat`.

```sh
./gradlew check
./gradlew -PaverionTarget=1.16.5 build
./gradlew -PaverionTarget=26.3 build
```

Find production jars in each target's `build/libs/` directory. Names take the form `averion-client-<version>-mc<minecraft>.jar`. Use Java 8 for a production 1.16.5 profile and Java 25 for a 26.3 profile. Both require Fabric Loader 0.20.1. Place the correct Averion jar and the matching Fabric API release in the profile's `mods/` directory.

| Minecraft | Game Java | Required Fabric API release |
| --- | --- | --- |
| 1.16.5 | 8 | [0.42.0+1.16](https://modrinth.com/mod/fabric-api/version/IQ3UGSc2) |
| 26.3 | 25 | [0.166.2+26.3](https://modrinth.com/mod/fabric-api/version/Zns5SSRA) |

For an IDE or local development smoke test, run the selected target's `runClient` task. Loom development runs use JDK 25 and a target-local run directory. For example, run `./gradlew -PaverionTarget=26.3 :targets:26.3:runClient`. Validate production jars on each declared game runtime separately.

The project lockfiles pin Gradle compile and test classpaths. The wrapper verifies its distribution checksum. Loom resolves Minecraft, mappings and launch dependencies from the selected version and upstream repositories. Update dependency pins deliberately and regenerate lock state with `./gradlew --write-locks build`. Review the lockfile diff before accepting updates.

## Repository layout

- `shared/src/client/` contains the shared entrypoint, feature runtime and client settings.
- `targets/` contains per-game build configuration and Fabric adapters.
- `distribution/` contains the launcher manifest schema and a synthetic example.
- `docs/` records architecture, research and validation.

Read the [architecture](docs/ARCHITECTURE.md), [version strategy](docs/MULTI_VERSION_SUPPORT.md), [launcher contract](docs/LAUNCHER_INTEGRATION.md) and [technical decisions](docs/TECHNICAL_DECISIONS.md). The project license is pending an owner decision.
