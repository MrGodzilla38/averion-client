# Client architecture

Averion Client is a client-only Fabric mod with a separate jar for each enabled Minecraft version. Averion owns feature behavior, settings and presentation. Averion Launcher owns profile creation, account handling, game and mod installation, and process launch.

## Code and dependency direction

`shared/src/client/java` contains the Fabric entrypoint and Java 8-compatible runtime. It is compiled into each target. The shared runtime owns feature state, key action dispatch, JSON settings and the HUD and ClickGUI layout. The target catalog selects `ClientHooks` and the relevant source adapters for that version. Gradle compiles them against the exact game and Fabric API pins. The adapters register lifecycle, key mapping, and rendering hooks. They translate native callbacks into the shared `HudText`, `GuiCanvas`, and `GuiInput` contracts. Minecraft types stay in target code. Yarn targets use a merged development game jar and Loom remapping. Unobfuscated targets use the split client source set. See the [version inventory](MULTI_VERSION_SUPPORT.md) for the adapter boundaries and game Java requirements.

| Responsibility | Ownership and boundary |
| --- | --- |
| Bootstrap and lifecycle | `AverionClient` reads Fabric metadata and constructs one `ClientRuntime`. The runtime loads settings and applies feature state. Target hooks dispatch client-thread events and call shutdown. Runtime shutdown clears callbacks' state and flushes the configuration writer. |
| Feature registration | An explicit runtime registration list uses stable IDs and rejects duplicates. Features hold metadata, enabled state, and typed settings. They can react to state transitions. The HUD category registers `hud` and `fps`. |
| HUD | Shared feature code chooses content and style. Target adapters check gameplay visibility and provide the native text renderer for the callback. |
| ClickGUI | One `ClickGui` session owns layout, filtering, selection, scrolling, hit testing, and value editing. Native screen adapters supply size, font measurements, pointer and keyboard events, and a synchronous drawing context. The world HUD and the settings screen have separate render entry points. |
| Keybindings | Shared bindings own action dispatch and repeat handling. Targets register normal Minecraft bindings. Minecraft persists remaps in its options file. The opening action runs in focused gameplay. Once open, the native screen owns input and `GuiInput` tracks a fresh press and release for closing. |
| Configuration | `SettingsStore` owns the schema, defaults and disk representation in the isolated game's config directory. `ClientRuntime` owns the small asynchronous writer and applies the cached live state. |
| Notifications | The shared runtime retains a single timed notice. The target paints it through the same HUD path when active play is visible. |
| Logging and errors | The named Averion logger records startup and config failures. Optional config errors fall back to defaults or session state. Core registration failures surface through Fabric. |
| Updates and distribution | Build metadata describes exact target requirements. The launcher verifies and manages the install transaction. See [Launcher integration](LAUNCHER_INTEGRATION.md). |

## Future integrations

Keep feature dependencies on small shared contracts with Minecraft-facing adapters at the target boundary. Register global hooks once and release world-scoped resources when disconnecting. Use namespaced original assets under `assets/averion_client/` and invalidate resource caches on reload if a later feature introduces them. Prefer Fabric hooks to mixins or access changes. Optional mod bridges should detect the exact capability before loading a class that references it. Any future networking should negotiate server support, validate bounded versioned payloads and dispatch onto the client thread.

ClickGUI draws visible rows through the native font and rectangle APIs. Search results and settings lists update on input. Feature edits take effect on the client thread, with coalesced configuration writes on one worker. A future HUD Editor can use its own native screen and reuse the canvas and runtime setting APIs. Keep its placement and preview state in that screen. [Fabric Loader](https://docs.fabricmc.net/develop/loader/), [Fabric events](https://docs.fabricmc.net/develop/events/), [Fabric Loom](https://docs.fabricmc.net/develop/loom/).

See the [feature system](FEATURE_SYSTEM.md) and [configuration format](CONFIGURATION.md) for extension points and behavior.
