# Feature system

Averion's internal feature registry owns one instance per stable ID for the lifetime of the client. The HUD category contains `hud` and `fps`. Category order is HUD, Render, Performance, Movement, Misc, and Client. ClickGUI shows categories with registered features. A feature has an ID, display name, description, category enabled state, and a list of typed settings. IDs start with a lowercase letter and can contain lowercase letters, digits and underscores. Duplicate registration fails before replacing the original.

## Lifecycle

The Fabric entrypoint loads profile settings and creates the runtime once. The runtime registers the bundled features and applies their saved or default state. Target adapters bind the game's key mapping, end-of-client-tick, HUD and client-stopping hooks once. A real state change invokes the feature's `onEnabledChanged` hook. A feature can override it to acquire or release resources. Do not perform disk or network work inside that callback when it runs during gameplay.

On game shutdown, the runtime clears the key actions and active notice, disables registered features and lets the configuration writer finish the last queued state. If a feature throws during shutdown, the registry logs its ID and continues with the other features. Feature hooks and rendering operate on the client thread. Keep world references owned by target code and release them on disconnect if a later feature needs them.

## ClickGUI and input

Press **Right Shift** in a focused world to open the ClickGUI. Press and release the same binding again, press **Esc**, or use **×** to close. Minecraft's native screen handles the mouse and suppresses gameplay input while open. The client checks the GUI mapping for duplicates in Controls before opening and ignores repeats from the opening press. Averion mappings are under **Averion Client** in **Options → Controls → Key Binds**. Older versions list them directly under **Controls**.

The translucent panels sit over a blurred game world. On versions with a menu blur setting, Minecraft controls the blur strength.

Choose a category on the left. Each module card pairs an icon with a live HUD preview. The caption band turns blue when the feature is enabled, and the switch also indicates state by its position. Click a card to open its settings on the right or use its switch to toggle it directly. The FPS counter defaults to disabled and measures rendered frames over one-second windows. It has independent color and shadow settings. Search matches a feature's name, ID, description, or category across the registry. Click the search field or use **Ctrl+F** (**Cmd+F** on macOS). The settings column contains the feature's enable switch and registered controls. Use the wheel over a column to see more rows. Click an enum row to advance and right-click to go back. Drag numeric sliders, and click a color row to expand its red, green, and blue sliders.

## HUD

The HUD defaults to two shadowed lines in the upper-left corner with **Averion Client** and the Minecraft version reported by Fabric Loader. The ClickGUI settings select the content, control the shadow and spacing, and set the version text color. Pause, chat, inventory, overlays, the debug display, and Minecraft's F1 hide-HUD setting suppress the display. Averion's toggle leaves the vanilla HUD enabled.

**F8** still toggles the branding HUD in focused gameplay through the same feature state as ClickGUI. A notice confirms the change for 2.5 seconds, including when the Averion HUD is disabled. The notice clears when exiting active play. Averion reports save failures in the ClickGUI and game log, with a notice after returning to gameplay.

## HUD Editor

Open ClickGUI and click **HUD Editor** in the left sidebar. To open the editor directly from gameplay, assign **Open Averion HUD Editor** in Minecraft Controls. The mapping starts unassigned. Click an element to select it and drag it over the game view. The editor shows only enabled HUD elements. The element-name button cycles through those elements so you can select overlapping ones. **Delete** removes the selected element from the draft. Saving disables it and keeps its placement and style for when you re-enable it from its ClickGUI module card. The horizontal and vertical alignment buttons snap it to the chosen screen edge or center. Drag again to adjust its offset from that anchor. Use **−** and **+** to resize it.

**Save & back** applies the draft and saves the active layout for the profile. **Esc** discards the draft and returns to ClickGUI. The mapped ClickGUI closing gesture closes the screen and discards any open draft. Content and style controls remain on the ClickGUI module cards. Startup restores the saved layout together with feature visibility and style.

## Adding a feature

1. Implement a shared Java 8-compatible feature with its own ID, metadata, state and behavior. Use a category that describes a real feature.
2. Register it explicitly during runtime composition. Apply its settings before activating it. Declare controls with `Feature.setting` and stable setting IDs. The existing types are `Toggle`, integer `Slider`, enum `Choice`, and RGB `Color`. The GUI renders them directly from the registry.
3. For a HUD element, extend `HudElement`, supply a default position and emit content in element-local GUI coordinates through `drawContent(HudText)`. The runtime uses the registered element for gameplay rendering, ClickGUI preview, editor controls and placement persistence. Keep Minecraft types and version-specific Fabric registration in the adapters.
4. Test state and data in the shared test suite, then exercise the built jar in each game's declared runtime.

Averion feature classes and adapters are internal. Fabric metadata and the launcher distribution manifest are the external boundaries.
