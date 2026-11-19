# Client architecture

Averion Client is a client-only Fabric mod with a separate jar for each enabled Minecraft version. Averion owns feature behavior, settings and presentation. Averion Launcher owns profile creation, account handling, game and mod installation, and process launch.

## Code and dependency direction

`shared/src/client/java` contains the Fabric entrypoint and Java 8-compatible runtime. It is compiled into each target. The shared runtime owns feature state, key action dispatch, JSON settings and the HUD layout. Each target supplies a `ClientHooks` adapter compiled against its game and Fabric API version. It registers the lifecycle, key mapping and rendering hooks and implements the shared `HudText` interface. Minecraft types stay in target code. The 1.16.5 Loom build uses a merged development game jar with client-only mod metadata. The 26.3 build uses Loom's split client source set.

| Responsibility | Ownership and boundary |
| --- | --- |
| Bootstrap and lifecycle | `AverionClient` reads Fabric metadata and constructs one `ClientRuntime`. The runtime loads settings and applies feature state. Target hooks dispatch client-thread events and call shutdown. Runtime shutdown clears callbacks' state and flushes the configuration writer. |
| Feature registration | An explicit runtime registration list uses stable IDs and rejects duplicates. Features hold metadata and enabled state and can react to state transitions. Only `hud` is registered. |
| HUD and interface | Shared code decides which text to draw and where. Target adapters decide whether gameplay is visible and pass the correct game renderer synchronously. Render contexts do not outlive the callback. |
| Keybindings | Shared bindings own action dispatch and repeat handling. Targets register normal Minecraft bindings. Minecraft persists remaps in its options file. |
| Configuration | `SettingsStore` owns the schema, defaults and disk representation in the isolated game's config directory. `ClientRuntime` owns the small asynchronous writer and applies the cached live state. |
| Notifications | The shared runtime retains a single timed notice. The target paints it through the same HUD path when active play is visible. |
| Logging and errors | The named Averion logger records startup and config failures. Optional config errors fall back to defaults or session state. Core registration failures surface through Fabric. |
| Updates and distribution | Build metadata describes exact target requirements. The launcher verifies and manages the install transaction. See [Launcher integration](LAUNCHER_INTEGRATION.md). |

## Future integrations

Keep feature dependencies on small shared contracts with Minecraft-facing adapters at the target boundary. Register global hooks once and release world-scoped resources when disconnecting. Use namespaced original assets under `assets/averion_client/` and invalidate resource caches on reload if a later feature introduces them. Prefer Fabric hooks to mixins or access changes. Optional mod bridges should detect the exact capability before loading a class that references it. Any future networking should negotiate server support, validate bounded versioned payloads and dispatch onto the client thread.

The Phase 01 hot paths consume one normal end-tick input queue and draw at most four short text lines. Text is cached or generated on state changes. Rendering performs no config reads, constructs no Averion render objects per frame, and calls only the game's text API. Pending saves are coalesced, and the notice has a single slot. These are design choices. Performance benchmarking remains separate. [Fabric Loader](https://docs.fabricmc.net/develop/loader/), [Fabric events](https://docs.fabricmc.net/develop/events/), [Fabric Loom](https://docs.fabricmc.net/develop/loom/).

See the [feature system](FEATURE_SYSTEM.md) and [configuration format](CONFIGURATION.md) for extension points and behavior.
