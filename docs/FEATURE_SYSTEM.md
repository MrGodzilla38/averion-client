# Feature system

Averion's internal feature registry owns one instance per stable ID for the lifetime of the client. Phase 01 registers only `hud`, under the HUD category. A feature has an ID, display name, description, category and enabled state. IDs start with a lowercase letter and can contain lowercase letters, digits and underscores. Duplicate registration fails before replacing the original.

## Lifecycle

The Fabric entrypoint loads profile settings and creates the runtime once. The runtime registers the bundled feature and applies its saved or default state. Target adapters bind the game's key mapping, end-of-client-tick, HUD and client-stopping hooks once. A real state change invokes the feature's `onEnabledChanged` hook. A feature can override it to acquire or release resources. Do not perform disk or network work inside that callback when it runs during gameplay.

On game shutdown, the runtime clears the key actions and active notice, disables registered features and lets the configuration writer finish the last queued state. If a feature throws during shutdown, the registry logs its ID and continues with the other features. Feature hooks and rendering operate on the client thread. Keep world references owned by target code and release them on disconnect if a later feature needs them.

## HUD and input

The HUD shows **Averion Client** and the Minecraft version reported by Fabric Loader. It uses two shadowed text lines in the upper-left corner, only during gameplay. Pause, chat, inventory, game overlays, the debug display and Minecraft's F1 hide-HUD setting suppress it. Toggling does not affect the vanilla HUD.

Press **F8** in a focused world to toggle it. Change the binding in **Options → Controls → Key Binds → Averion Client → Toggle Averion HUD**. Older Minecraft versions list the binding directly on the Controls screen. The adapter registers a normal Minecraft mapping. The runtime consumes queued clicks once per client tick, suppresses held repeats and discards clicks while a screen is open or the window lacks focus. Minecraft's own options store the assigned key.

A small notification below the HUD confirms the toggle for 2.5 seconds even when the Averion HUD itself is disabled. Only the latest notice is retained. A notice supports a title, optional description, information or warning styling, and a monotonic expiry. It clears on leaving active play. Save failures are logged and surfaced through the same notice. [Fabric key mappings](https://docs.fabricmc.net/develop/key-mappings), [Fabric HUD rendering](https://docs.fabricmc.net/develop/rendering/hud).

## Adding a feature

1. Implement a shared Java 8-compatible feature with its own ID, metadata, state and behavior. Use a category that describes a real feature.
2. Register it explicitly during runtime composition. Apply its settings before activating it. Keep the feature's settings and validation with its behavior and extend the settings schema when the format changes incompatibly.
3. Reuse the existing input dispatcher or HUD text boundary when applicable. Keep Minecraft types and version-specific Fabric registration in each target. Add an adapter hook only when a feature uses it.
4. Test state and data in the shared test suite, then exercise the built jar in each game's declared runtime.

Averion feature classes and adapters are internal. Fabric metadata and the launcher distribution manifest are the external boundaries.
