# Technical decisions

| Decision | Evidence and rationale | Alternatives considered |
| --- | --- | --- |
| One repository, independently built modules per exact game version | 1.16.5 needs Java 8 bytecode and a remapped jar. 26.3 uses Java 25 and unobfuscated game names. Shared Java source keeps behavior together while Gradle subprojects isolate tool and dependency choices. | A universal jar forces version branching and binary compatibility across changing APIs. Separate repositories duplicate feature changes and release policy. |
| Start on 1.16.5, also build 26.3 | The compatibility floor constrains shared code and forces a legacy build. Mojang's newest stable release exercises the modern Loom pipeline. | Starting only with a middle version leaves both boundaries untested. |
| Fabric Loader and Loom | Loader supplies mod metadata, client entrypoints and runtime discovery. Loom supplies game dependencies, development launch and remapping. Pin stable tooling. Use the latest maintained hook for each target and add Fabric API only when required. | A custom bootstrap or forked game adds ownership and redistribution cost. Hard dependencies on Fabric API and optimization mods are unnecessary for this bootstrap. |
| Explicit feature wiring and target adapters | Small independently owned features can evolve without a service container or an extensive abstraction over Minecraft. | A single initializer for all behavior makes ports and compatibility checks harder. Runtime plugin discovery creates an additional compatibility surface. |
| Exact releases and signed manifests | GitHub Releases provides an initial index and asset store. Launcher verifies the signed manifest and stages exact dependencies and artifacts per isolated profile. | Deriving trust from a checksum in release notes or installing by filename alone loses publisher and version guarantees. |
| Direct upstream Modrinth resolution | Upstream file metadata provides game, loader, dependency and file hash data. Installation and user file ownership already belong to the launcher. | Bundling all optimization mods prevents independent selection and increases license exposure. |

Sources for these decisions include [Fabric Loom](https://docs.fabricmc.net/develop/loom/), [Mojang's game index](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), [GitHub Releases](https://docs.github.com/en/rest/releases/releases) and [Modrinth API](https://docs.modrinth.com/api/).

## Decisions for the project owner

- Select the Averion Client license and decide who may distribute or modify it. Phase 00 grants no project license. Establish contribution terms before inviting outside contributions.
- Confirm the official release repository and visibility, release approvers and signing-key custody and recovery. The provided GitHub path could not be read anonymously during Phase 00. Publishing also needs launcher-side verification and a private signing key kept outside artifacts.
- Approve any redistribution, fork or bundled preset of third-party mods after reviewing the exact version's license and upstream terms. The default architecture uses direct downloads from upstream and ships Averion artifacts only.
