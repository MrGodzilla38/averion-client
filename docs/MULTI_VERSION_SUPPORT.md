# Minecraft version support

The inventory includes all 40 entries that Mojang marks as stable releases from `1.16` through `26.3`, inclusive. Mojang's ID for the first release is `1.16`. This is also the Yarn, launcher and jar filename ID. Gradle accepts `-PaverionTarget=1.16.0` as an alias. [Mojang release index and per-version metadata](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json).

## Build and compatibility matrix

Every target uses **JDK 25, Gradle 9.6.0, and Fabric Loom 1.17.21** for builds. Loom 1.17.21 declares Java 21 and Gradle plugin API 9.5.0. This repository uses JDK 25 to compile every target, including the Minecraft 26.x releases. The Java column is the game runtime major from Mojang's metadata and the bytecode target used for that jar. Loader pins were checked against Fabric Meta per game ID. Fabric API pins come from releases listed for each exact game ID and cross-checked against Fabric Maven. Each POM supplies the selected module versions. The [target catalog](../gradle/minecraft-targets.json) drives Gradle and the CI matrix. The [example manifest](../distribution/manifest-v1.example.json) shows launcher pins and target artifact names. [Fabric Meta](https://meta.fabricmc.net/), [Fabric Maven](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/), [Loom module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.module), [Gradle Java compatibility](https://docs.gradle.org/9.6.0/userguide/compatibility.html), [Fabric API version catalog](https://modrinth.com/mod/fabric-api/versions).

| Minecraft ID | Game Java | Fabric Loader | Fabric API | Mappings | Build and JUnit | Gameplay |
| --- | --- | --- | --- | --- | --- | --- |
| 1.16 | 8 | 0.20.1 | [`0.13.1+build.370-1.16`](https://modrinth.com/mod/fabric-api/version/RtAjPwHu) | Yarn `1.16+build.4` | Passed | Not run |
| 1.16.1 | 8 | 0.20.1 | [`0.18.0+build.387-1.16.1`](https://modrinth.com/mod/fabric-api/version/qPNxv5qZ) | Yarn `1.16.1+build.21` | Passed | Not run |
| 1.16.2 | 8 | 0.20.1 | [`0.20.1+build.401-1.16`](https://modrinth.com/mod/fabric-api/version/ybc5Fjql) | Yarn `1.16.2+build.47` | Passed | Not run |
| 1.16.3 | 8 | 0.20.1 | [`0.25.0+build.415-1.16`](https://modrinth.com/mod/fabric-api/version/ClXHxfOV) | Yarn `1.16.3+build.47` | Passed | Not run |
| 1.16.4 | 8 | 0.20.1 | [`0.29.3+1.16`](https://modrinth.com/mod/fabric-api/version/YC7ealNj) | Yarn `1.16.4+build.9` | Passed | Not run |
| 1.16.5 | 8 | 0.20.1 | [`0.42.0+1.16`](https://modrinth.com/mod/fabric-api/version/IQ3UGSc2) | Yarn `1.16.5+build.10` | Passed | Phase 01 gameplay |
| 1.17 | 16 | 0.20.1 | [`0.36.0+1.17`](https://modrinth.com/mod/fabric-api/version/nSk00F5M) | Yarn `1.17+build.13` | Passed | Not run |
| 1.17.1 | 16 | 0.20.1 | [`0.46.1+1.17`](https://modrinth.com/mod/fabric-api/version/31ES0yWr) | Yarn `1.17.1+build.65` | Passed | Not run |
| 1.18 | 17 | 0.20.1 | [`0.44.0+1.18`](https://modrinth.com/mod/fabric-api/version/RSZHmY2f) | Yarn `1.18+build.1` | Passed | Not run |
| 1.18.1 | 17 | 0.20.1 | [`0.46.6+1.18`](https://modrinth.com/mod/fabric-api/version/kbtdliUm) | Yarn `1.18.1+build.22` | Passed | Not run |
| 1.18.2 | 17 | 0.20.1 | [`0.77.0+1.18.2`](https://modrinth.com/mod/fabric-api/version/qk28POfr) | Yarn `1.18.2+build.4` | Passed | Not run |
| 1.19 | 17 | 0.20.1 | [`0.58.0+1.19`](https://modrinth.com/mod/fabric-api/version/3KmOcp6b) | Yarn `1.19+build.4` | Passed | Not run |
| 1.19.1 | 17 | 0.20.1 | [`0.58.5+1.19.1`](https://modrinth.com/mod/fabric-api/version/3fY6NddT) | Yarn `1.19.1+build.6` | Passed | Not run |
| 1.19.2 | 17 | 0.20.1 | [`0.77.0+1.19.2`](https://modrinth.com/mod/fabric-api/version/6g95K303) | Yarn `1.19.2+build.28` | Passed | Not run |
| 1.19.3 | 17 | 0.20.1 | [`0.76.1+1.19.3`](https://modrinth.com/mod/fabric-api/version/jyKnHEDY) | Yarn `1.19.3+build.5` | Passed | Not run |
| 1.19.4 | 17 | 0.20.1 | [`0.87.2+1.19.4`](https://modrinth.com/mod/fabric-api/version/nyAmoHlr) | Yarn `1.19.4+build.2` | Passed | Not run |
| 1.20 | 17 | 0.20.1 | [`0.83.0+1.20`](https://modrinth.com/mod/fabric-api/version/n2c5lxAo) | Yarn `1.20+build.1` | Passed | Not run |
| 1.20.1 | 17 | 0.20.1 | [`0.92.12+1.20.1`](https://modrinth.com/mod/fabric-api/version/rvI2dfzR) | Yarn `1.20.1+build.10` | Passed | Not run |
| 1.20.2 | 17 | 0.20.1 | [`0.91.6+1.20.2`](https://modrinth.com/mod/fabric-api/version/8GVp7wDk) | Yarn `1.20.2+build.4` | Passed | Not run |
| 1.20.3 | 17 | 0.20.1 | [`0.91.1+1.20.3`](https://modrinth.com/mod/fabric-api/version/Yolngp3s) | Yarn `1.20.3+build.1` | Passed | Not run |
| 1.20.4 | 17 | 0.20.1 | [`0.97.3+1.20.4`](https://modrinth.com/mod/fabric-api/version/BPX6fK06) | Yarn `1.20.4+build.3` | Passed | Not run |
| 1.20.5 | 21 | 0.20.1 | [`0.97.8+1.20.5`](https://modrinth.com/mod/fabric-api/version/GCdY4I8I) | Yarn `1.20.5+build.1` | Passed | Not run |
| 1.20.6 | 21 | 0.20.1 | [`0.100.8+1.20.6`](https://modrinth.com/mod/fabric-api/version/ocg4hG3t) | Yarn `1.20.6+build.3` | Passed | Not run |
| 1.21 | 21 | 0.20.1 | [`0.102.0+1.21`](https://modrinth.com/mod/fabric-api/version/oGwyXeEI) | Yarn `1.21+build.9` | Passed | Not run |
| 1.21.1 | 21 | 0.20.1 | [`0.116.18+1.21.1`](https://modrinth.com/mod/fabric-api/version/5SwzqWUu) | Yarn `1.21.1+build.3` | Passed | Not run |
| 1.21.2 | 21 | 0.20.1 | [`0.106.1+1.21.2`](https://modrinth.com/mod/fabric-api/version/UEjZZNue) | Yarn `1.21.2+build.1` | Passed | Not run |
| 1.21.3 | 21 | 0.20.1 | [`0.114.1+1.21.3`](https://modrinth.com/mod/fabric-api/version/wLq4rGsB) | Yarn `1.21.3+build.2` | Passed | Not run |
| 1.21.4 | 21 | 0.20.1 | [`0.119.4+1.21.4`](https://modrinth.com/mod/fabric-api/version/p96k10UR) | Yarn `1.21.4+build.8` | Passed | Not run |
| 1.21.5 | 21 | 0.20.1 | [`0.128.2+1.21.5`](https://modrinth.com/mod/fabric-api/version/kKEGlsne) | Yarn `1.21.5+build.1` | Passed | Not run |
| 1.21.6 | 21 | 0.20.1 | [`0.128.2+1.21.6`](https://modrinth.com/mod/fabric-api/version/F5TVHWcE) | Yarn `1.21.6+build.1` | Passed | Not run |
| 1.21.7 | 21 | 0.20.1 | [`0.129.0+1.21.7`](https://modrinth.com/mod/fabric-api/version/JntuF9Ul) | Yarn `1.21.7+build.8` | Passed | Not run |
| 1.21.8 | 21 | 0.20.1 | [`0.136.1+1.21.8`](https://modrinth.com/mod/fabric-api/version/g58ofrov) | Yarn `1.21.8+build.1` | Passed | Not run |
| 1.21.9 | 21 | 0.20.1 | [`0.134.1+1.21.9`](https://modrinth.com/mod/fabric-api/version/vczmWJBj) | Yarn `1.21.9+build.1` | Passed | Not run |
| 1.21.10 | 21 | 0.20.1 | [`0.138.4+1.21.10`](https://modrinth.com/mod/fabric-api/version/tV4Gc0Zo) | Yarn `1.21.10+build.3` | Passed | Not run |
| 1.21.11 | 21 | 0.20.1 | [`0.141.6+1.21.11`](https://modrinth.com/mod/fabric-api/version/6qAuTtLR) | Yarn `1.21.11+build.6` | Passed | Not run |
| 26.1 | 25 | 0.20.1 | [`0.155.3+26.1.2`](https://modrinth.com/mod/fabric-api/version/3dM0X6ou) | Native Mojang names | Passed | Not run |
| 26.1.1 | 25 | 0.20.1 | [`0.155.3+26.1.2`](https://modrinth.com/mod/fabric-api/version/3dM0X6ou) | Native Mojang names | Passed | Not run |
| 26.1.2 | 25 | 0.20.1 | [`0.155.3+26.1.2`](https://modrinth.com/mod/fabric-api/version/3dM0X6ou) | Native Mojang names | Passed | Not run |
| 26.2 | 25 | 0.20.1 | [`0.161.0+26.2`](https://modrinth.com/mod/fabric-api/version/ewUK83HI) | Native Mojang names | Passed | Not run |
| 26.3 | 25 | 0.20.1 | [`0.166.2+26.3`](https://modrinth.com/mod/fabric-api/version/Zns5SSRA) | Native Mojang names | Passed | Phase 01 gameplay |

## Verification results

All 40 targets passed local Gradle builds, production JAR checks, and 21 shared JUnit cases each (840 total). The default build also passed offline for the entire catalog using the recorded dependency locks. The JARs were checked against the exact Fabric API bundles in the distribution example. The Gameplay column records historical Phase 01 runs for 1.16.5 and 26.3. Verification of the current JARs was limited to build, unit and package checks. [Phase 01 gameplay steps and results](PHASE_01_REPORT.md).

## Integration boundaries

- **1.16–1.19.4** uses the Fabric matrix-stack HUD callback. Older Yarn builds use a different keybinding package. The first release uses Fabric's v0 client tick event and a client-stop mixin to flush settings because its API bundle has no v1 stopping event.
- **1.20–1.21.5** uses the DrawContext HUD callback. Starting in 1.20.2, debug visibility comes from Minecraft's debug HUD.
- **1.21.6–1.21.11** registers a Fabric HUD layer. Starting in 1.21.9, the Averion Controls entry uses an identified keybinding category.
- **26.1–26.3** uses unobfuscated game names and GuiGraphicsExtractor. Starting in 26.2, screen and overlay state come from Minecraft's `Gui` object. [Fabric HUD registry](https://maven.fabricmc.net/docs/fabric-api-0.129.0%2B1.21.7/net/fabricmc/fabric/api/client/rendering/v1/hud/HudElementRegistry.html), [Fabric mapping transition](https://fabricmc.net/2026/03/14/261.html), [Fabric 26.2 GUI changes](https://fabricmc.net/2026/06/15/262.html).

ClickGUI uses a native screen on every target. The canvas adapters cover MatrixStack through 1.19.4, DrawContext from 1.20 through 1.21.11, and GuiGraphicsExtractor from 26.1 onward. The Yarn input adapters handle primitive callbacks through 1.21.8 and event objects from 1.21.9. The unobfuscated adapter uses Minecraft's own key and button constants to handle 26.3's switch to SDL input.

Gradle compiles the shared ClickGUI and HUD layout, input actions, notifications, settings, and lifecycle state for every target. Targets through 1.21.11 use Loom's remapping plugin and produce intermediary production jars. Starting in 26.1, targets use Loom's unobfuscated plugin and produce native-name jars. Every production file is named `averion-client-<minecraftId>.jar` and carries the exact Minecraft dependency and the independent Averion release version.

## Repeating verification

Use `./gradlew -PaverionTarget=<minecraftId> build` for one release. A comma-separated list selects a batch of independent targets. Each build runs the shared JUnit suite and validates its production jar's metadata, filename and bytecode. Run the packaged game on the Java version in the matrix to verify rendering, F8, Controls remapping, and persistence. Historical full gameplay runs are recorded in the [Phase 01 report](PHASE_01_REPORT.md).
