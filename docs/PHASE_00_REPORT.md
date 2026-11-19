# Phase 00 report

Phase 00 established the Averion Client foundation in an empty workspace. Minecraft **1.16.5** is the first target and **26.3** is the second. Both build separate production jars. Both loaded the packaged Averion mod under Fabric on Linux x86-64 and logged the initializer.

## Architecture selected

Use one repository with one Gradle subproject and one artifact per exact game version. Share the client bootstrap and future Minecraft-independent feature logic as Java source. Keep platform bindings with each target. The older target exercises Java 8 bytecode and Loom's remapping mode. The newer target exercises Loom's unobfuscated mode, split client sources and Java 25. Loom 1.17.21 runs under Gradle 9.6.0 on JDK 25. Fabric Loader 0.20.1 is pinned for both. The bootstrap needs no Fabric API or third-party mod. [Architecture](ARCHITECTURE.md), [Version support](MULTI_VERSION_SUPPORT.md).

The launcher contract uses a signed versioned manifest, exact per-target artifacts and dependencies, separate release channels, and staged profile updates with recovery and rollback. GitHub Releases is the proposed asset host. Averion Launcher owns Modrinth resolution and file management and uses upstream downloads. The client owns runtime features. [Launcher contract](LAUNCHER_INTEGRATION.md), [Modrinth integration](MODRINTH_INTEGRATION.md), [Security and distribution](SECURITY_AND_DISTRIBUTION.md).

The optimization review checked Sodium, Lithium, FerriteCore, ImmediatelyFast and Entity Culling. It found published Fabric builds on both targets for four candidates. ImmediatelyFast had a 26.3 result only. Upstream license terms and release-specific dependencies vary, including distinct licenses for legacy and current Sodium. These are candidates for independent compatibility tests and benchmarking. [Optimization strategy](OPTIMIZATION_STRATEGY.md).

## Files created

| Path | Purpose |
| --- | --- |
| `README.md`, `docs/*.md` | Project setup, architecture, version support, integration, optimization, distribution, decisions and this report |
| `settings.gradle`, `build.gradle`, `gradle.properties` | Target selection, aggregate build and formatting checks, version and Gradle JVM settings |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Gradle 9.6.0 wrapper and pinned distribution checksum |
| `gradle/client-target.gradle`, `targets/*/build.gradle`, `targets/*/gradle.lockfile` | Loom configuration, shared build rules, per-version pins and strict compile/test dependency locks |
| `shared/src/main/resources/fabric.mod.json` | Exact target metadata and the client-only entrypoint |
| `shared/src/client/java/dev/averion/client/AverionClient.java` | Fabric bootstrap and a startup log containing the mod and game version |
| `shared/src/test/java/dev/averion/client/AverionClientTest.java` | Parameterized startup diagnostic test |
| `distribution/manifest-v1.schema.json`, `distribution/manifest-v1.example.json` | JSON Schema and synthetic manifest example |
| `.github/workflows/ci.yml`, `.gitattributes`, `.gitignore` | Two-target CI matrix, wrapper validation, line endings and generated-file exclusions |

## Validation

The host ran Omarchy Linux x86-64. The build and tests used Amazon Corretto 25.0.5.1. Production smoke tests used Corretto 8.514.01.1 for 1.16.5 and Corretto 25.0.5.1 for 26.3. The wrapper jar and Gradle ZIP matched the official SHA-256 values. [Gradle checksums](https://gradle.org/release-checksums/).

```sh
./gradlew --no-daemon --console=plain --write-locks build
./gradlew --no-daemon --console=plain --offline --rerun-tasks build
./gradlew --no-daemon --console=plain --offline -PaverionTarget=1.16.5 build
./gradlew --no-daemon --console=plain --offline -PaverionTarget=26.3 build
sha256sum targets/*/build/libs/*.jar
actionlint -color=false .github/workflows/ci.yml
```

The lock-generating build, forced offline rebuild and both isolated target builds passed. The checks included whitespace, `javac -Xlint:all -Werror` with the deliberate Java 8 compiler-option warning suppressed on 1.16.5, two JUnit cases per target, and production jar inspection. Jar verification checked archive names, client-only metadata, entrypoint class, exact game and Loader pins, Java classfile major and ownership of packaged classes. Both jar hashes matched the pre-rebuild hashes under the same JDK and cache. Actionlint 1.7.12 accepted the workflow. GitHub Actions itself was outside this local validation. A Python check with jsonschema 4.26.0 validated the schema and sample and rejected a channel/version mismatch and an unsupported schema version.

| Target | Compiled and packaged | Automated tests | Packaged mod runtime | In-game verification |
| --- | --- | --- | --- | --- |
| 1.16.5 | Passed, remapped jar, classfile 52 | 2 passed | Logged initialization and reached the title screen on Java 8 | No world or multiplayer session exercised |
| 26.3 | Passed, unobfuscated jar, classfile 69 | 2 passed | Logged initialization and reached the first-run Welcome screen on Java 25 | No world or multiplayer session exercised |

Runtime evidence came from local isolated Fabric profiles using Mojang libraries and game downloads, the built jar in the profile's `mods/` directory, Xvfb and software OpenGL. The development smoke used local placeholder account data and partial assets for startup verification. It covered neither account authentication nor audio, and the environment lacked native narration support. The first 26.3 attempt ran the entrypoint but could not complete resource reload. The retry staged the official non-audio default-locale assets and reached the Welcome screen. Local scripts drove these checks in isolated Fabric profiles.

The 1.16.5 build initially rejected Loom's split environment layout because the version predates bundled server jars. The target now uses merged development inputs with client-only mod metadata. JDK 25's obsolete-target warning and an interpolation error in the jar verifier were fixed before final verification.

## Remaining scope and decisions

Feature implementations, launcher-side manifest verification and installs, Modrinth resolution, release signing and publishing, and ports to intermediate game versions are outside Phase 00. Full gameplay, Windows, macOS, Vulkan and third-party mod combinations remain unverified. The project owner must choose the Averion license, confirm the release repository and visibility, assign key custody and approve any third-party redistribution. The local directory contained no Git checkout, and the specified GitHub API repository returned HTTP 404 anonymously. No Git history, remote release, launcher files or proprietary game assets were changed or added. [Technical decisions](TECHNICAL_DECISIONS.md).
