# Averion Client

Averion Client is the Fabric-based Minecraft Java Edition client for the Averion ecosystem. It hosts Averion's own gameplay-facing client features and integrations. The separate Averion Launcher manages profiles, accounts, downloads and launch.

The target catalog covers all **40 stable Minecraft Java Edition releases** from **1.16** (also called 1.16.0) through **26.3**. The [compatibility matrix](docs/MULTI_VERSION_SUPPORT.md) lists the game Java and Fabric API pins and distinguishes build results from gameplay verification.

## HUD Editor

Open ClickGUI with **Right Shift**, then click **HUD Editor** in the left sidebar. Its optional keybind is unassigned by default. The editor shows enabled HUD elements. Drag to position them, use the controls to change scale or alignment, or use **Delete** to disable the selected element when you save. Re-enable it from its module card. **Save & back** applies the layout and saves it for the profile. **Esc** discards the draft and returns to ClickGUI. [Controls and extension points](docs/FEATURE_SYSTEM.md), [Phase 03 verification](docs/PHASE_03_REPORT.md).

## Build

Install JDK 25 and set `JAVA_HOME` to it. Use the checked-in Gradle 9.6.0 wrapper. The first build needs network access to download Minecraft development files and build dependencies. On Windows, use `gradlew.bat`.

```sh
./gradlew -PaverionTarget=1.16.5 build
./gradlew -PaverionTarget=26.3 build
./gradlew -PaverionTarget=1.20.1,1.21.1 build
```

Omit `-PaverionTarget` to select the entire catalog. Building selected versions limits game downloads, memory and build time. Use `1.16` as the game ID and artifact version for the first release. The command line also accepts `1.16.0` for that target.

Find each production jar at `targets/<minecraft>/build/libs/averion-client-<minecraft>.jar`. The Averion release version is stored in `gradle.properties`, packaged mod metadata, and the release manifest and tag. Install the matching jar and the Fabric API release listed in the compatibility matrix under the profile's `mods/` directory. Use Fabric Loader 0.20.1 and the Java version in that row.

Production builds remove obsolete phase-named jars from generated output directories. Loom puts development jars for remapped targets under `build/devlibs/`.

For an IDE or local development smoke test, run the selected target's `runClient` task. Loom development runs use JDK 25 and a target-local run directory. For example, run `./gradlew -PaverionTarget=26.3 :targets:26.3:runClient`. Validate production jars on each declared game runtime separately.

The project lockfiles pin Gradle compile and test classpaths. The wrapper verifies its distribution checksum. Loom resolves Minecraft, mappings and launch dependencies from the selected version and upstream repositories. Update dependency pins deliberately and regenerate lock state with `./gradlew --write-locks build`. Review the lockfile diff before accepting updates.

## Repository layout

- `shared/src/client/` contains the shared entrypoint, feature runtime and client settings.
- `gradle/minecraft-targets.json` pins the exact game profiles.
- `adapters/` contains shared Fabric integration code for API families.
- `targets/` contains isolated builds, dependency locks, and any target-specific code or resources.
- `distribution/` contains the launcher manifest schema and a synthetic example.
- `docs/` records architecture, research and validation.

Read the [architecture](docs/ARCHITECTURE.md), [version strategy](docs/MULTI_VERSION_SUPPORT.md), [launcher contract](docs/LAUNCHER_INTEGRATION.md) and [technical decisions](docs/TECHNICAL_DECISIONS.md). The project license is pending an owner decision.
