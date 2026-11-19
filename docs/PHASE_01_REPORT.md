# Phase 01 report

Phase 01, originally validated as Averion Client `0.1.0-phase.1`, implements a profile-scoped feature runtime and its first gameplay feature. The HUD shows Averion Client branding and the running Minecraft version. It defaults to enabled. F8 toggles it during play and shows a short confirmation. The setting is stored in `<game directory>/config/averion-client.json`. Minecraft's Controls UI manages the mapping and stores remaps in `options.txt`. [Feature system](FEATURE_SYSTEM.md), [Configuration](CONFIGURATION.md).

## Architecture and changes

- The Fabric entrypoint starts one runtime. A registry owns feature IDs and state, rejects duplicates and deactivates enabled features on shutdown. Only the HUD is registered.
- The shared code targets the Java 8 API baseline. Minecraft 1.16.5 uses a Yarn-remapped jar and a matrix-stack HUD callback. Minecraft 26.3 uses a split client source set and the unobfuscated HUD element and graphics extraction APIs. Both use normal Fabric lifecycle and key registration hooks. [Architecture](ARCHITECTURE.md), [Version support](MULTI_VERSION_SUPPORT.md).
- State changes and failures use a single, expiring in-world notice. Configuration uses Minecraft's existing Gson, a bounded file read, preservation of invalid and newer files, and a coalesced asynchronous atomic save. The runtime flushes pending writes on normal client exit.
- Maven dependencies added per target are the matching Fabric API base, lifecycle, key registration and rendering modules. The 26.3 rendering module also brings Fabric's transitive access wideners. Fabric's public hooks supply these integrations without adding Averion mixins. The tested installation uses the exact upstream Fabric API release alongside Averion. The Averion jars contain no bundled Fabric API. No dependencies were removed.

| Path | Change |
| --- | --- |
| `shared/src/client/java/dev/averion/client/AverionClient.java` | Guarded bootstrap and runtime setup |
| `shared/src/client/java/dev/averion/client/{ClientRuntime,Feature,FeatureRegistry,HudFeature,HudText,KeyBindings,Notifications,SettingsStore}.java` | Shared lifecycle and feature implementation |
| `targets/1.16.5/src/main/java/dev/averion/client/ClientHooks.java`, `targets/26.3/src/client/java/dev/averion/client/ClientHooks.java` | Minecraft version adapters |
| `shared/src/main/resources/fabric.mod.json`, `shared/src/main/resources/assets/averion_client/lang/en_us.json` | Runtime module predicates and Controls labels |
| `gradle.properties`, `gradle/client-target.gradle`, `targets/*/build.gradle`, `targets/*/gradle.lockfile` | Phase version, module pins and package checks |
| `shared/src/test/java/dev/averion/client/{ClientRuntime,FeatureRegistry,KeyBindings,Notifications,SettingsStore}Test.java` | Behavior and persistence tests |
| `README.md`, `docs/ARCHITECTURE.md`, `docs/MULTI_VERSION_SUPPORT.md`, `docs/FEATURE_SYSTEM.md`, `docs/CONFIGURATION.md`, `docs/PHASE_01_REPORT.md` | Setup, current runtime decisions and verification |

Phase 00's Gradle, Loom, Loader, mappings, bytecode targets and CI workflow still apply. Current production jars use the Minecraft target in the filename and carry Averion version `0.1.0` in mod metadata. The synthetic release manifest shows the current names and pins the matching upstream Fabric API file per target. The legacy bundle's root Fabric mod ID is `fabric` and the modern bundle uses `fabric-api`. [Launcher manifest contract](LAUNCHER_INTEGRATION.md).

## Target requirements

Gradle and JUnit use JDK 25 for both targets. Both game profiles require Fabric Loader 0.20.1.

| Minecraft | Game Java and bytecode | Required Fabric API release |
| --- | --- | --- |
| 1.16.5 | Java 8, classfile 52 | [0.42.0+1.16](https://modrinth.com/mod/fabric-api/version/IQ3UGSc2) |
| 26.3 | Java 25, classfile 69 | [0.166.2+26.3](https://modrinth.com/mod/fabric-api/version/Zns5SSRA) |

## Build and tests

The host ran Omarchy Linux x86-64 with Corretto 25.0.5.1. From the project root with `JAVA_HOME` set to an installed JDK 25, use these commands to repeat the build checks. Run the online build first to populate the Gradle cache.

```sh
./gradlew --no-daemon --console=plain -PaverionTarget=1.16.5,26.3 build
artifact_hashes="$(mktemp)"
sha256sum targets/1.16.5/build/libs/averion-client-1.16.5.jar \
  targets/26.3/build/libs/averion-client-26.3.jar > "$artifact_hashes"
./gradlew --no-daemon --offline --console=plain --max-workers=4 --rerun-tasks -PaverionTarget=1.16.5,26.3 build
sha256sum -c "$artifact_hashes"
./gradlew --no-daemon --offline --console=plain --max-workers=4 -PaverionTarget=1.16.5 build
./gradlew --no-daemon --offline --console=plain --max-workers=4 -PaverionTarget=26.3 build
actionlint -color=false .github/workflows/ci.yml
```

The original phase also ran `./gradlew --no-daemon --console=plain --write-locks build` to record the new dependency pins. Routine validation uses the existing lockfiles.

The lock-generating build, forced offline rebuild and both isolated builds passed. Both production jar SHA-256 values matched the first successful build in that workspace and cache. Actionlint accepted the CI workflow. Each target ran 21 JUnit cases with zero failures, errors or skips. The tests cover stable feature identification, duplicates, state transitions and cleanup, input repeat and screen behavior, defaults, serialization, corrupt-file recovery, compatibility with unknown fields and future schemas, notice replacement and expiry, and persistence across fresh runtime instances. The builds also run whitespace checks, strict compiler warnings and jar checks for classfile versions, the entrypoint, adapter, language resource and module requirements.

A later clean-copy build for GitHub preparation also passed all 21 tests per target. The 1.16.5 jar matched the previous jar byte for byte. The 26.3 jar had a different SHA-256 because Loom generated the same client-only manifest entries in a different order. The manifest values and all other jar entries matched. Use the hash of the final packaged file when preparing a release.

The initial compile exposed the 26.3 screen accessors on `Minecraft.gui` and an annotation warning from the game-provided `GsonBuilder`. The adapter now uses the correct screen accessors, and settings serialization uses the shared Gson streaming API. The strict compiler checks remain active.

## Packaged runtime verification

The launch harness installed the built jar and the corresponding SHA-512-verified Modrinth Fabric API release in isolated test profiles. It used Fabric Loader 0.20.1 and official Minecraft game data under Xvfb with software OpenGL. The test profiles used a local placeholder account and non-audio default-locale assets. The game JVMs were Corretto 8.514.01.1 for 1.16.5 and Corretto 25.0.5.1 for 26.3. Log output from both packaged artifacts included Averion initialization.

The local launch and interaction scripts were specific to the verification environment. To repeat the gameplay checks, install the built jar and matching Fabric API in an isolated profile for each target. Join a single-player world, toggle the HUD with F8, test F1 and pause behavior, then disable the HUD and rebind its toggle in Controls. Quit normally, reopen the same profile and world, and check that the HUD is still disabled and the remapped key enables it.

The interaction logs and captures were collected with X11 input and window capture on the isolated profiles. Both clients rendered their own Minecraft version in a local world, showed enable and disable notices, and hid the Averion HUD in the pause menu. F8 in that menu left the setting unchanged. Separate captures confirmed that F1 hid and restored the Averion and vanilla HUDs.

| Target | World and key checks | Restart |
| --- | --- | --- |
| 1.16.5 / Java 8 | Entered a creative world, toggled off and on with F8, and found the labelled binding in Controls. | Reopened the world with the HUD off, then enabled it with the saved F9 remap and saw the confirmation. |
| 26.3 / Java 25 | Entered a creative world, toggled off and on with F8, and found the labelled binding in Key Binds. | Loaded the saved disabled state, then enabled it with the saved F9 remap in the reopened world and saw the confirmation. |

Both profiles saved the disabled state and the F9 remap before quitting. The restarted clients accepted the saved remap, displayed the enable notice, and persisted the new enabled state. The remap was made only in the verification profiles. The packaged default is F8.

## Limits and owner decisions

Phase 01 adds no settings screen. The HUD setting is available through the binding and JSON file. No performance benefit has been measured. Multiplayer, account authentication, other operating systems, hardware graphics backends, third-party mod stacks and intermediate Minecraft versions are outside this verification. The Gradle CI workflow is present, and the checks ran locally. Hosted CI was not run.

No new owner decision is required to build or use Phase 01 locally. Public release still depends on the project license and release ownership and signing decisions recorded in Phase 00. The workspace had no Git checkout. No commits, pushes, release assets or launcher modifications were made. [Phase 00 report](PHASE_00_REPORT.md), [Technical decisions](TECHNICAL_DECISIONS.md).
