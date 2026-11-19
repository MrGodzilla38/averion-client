# Phase 02 report

Right Shift opens Averion's native ClickGUI during active gameplay. A second press and release, Esc, or the close button returns to the game. The screen shows categories with registered features, supports registry-wide search and scrolls each column independently. The initial release registered the branding HUD. The subsequent card layout adds a separately configurable FPS counter. Its enable switch and four settings change the live feature, and F8 continues to toggle it during play. [Feature system](FEATURE_SYSTEM.md), [Configuration](CONFIGURATION.md).

| Area | Changes |
| --- | --- |
| Shared GUI | `ClickGui` and `GuiCanvas` handle layout, filtering, navigation, scrolling, and settings editing. `GuiInput` handles screen shortcuts and the release gate. |
| Feature runtime | `Feature`, `FeatureRegistry`, `Setting`, and `ClientRuntime` expose registered settings, apply edits, and save a coalesced snapshot. `HudFeature` uses the settings in its normal renderer. |
| Minecraft adapters | Yarn matrix and draw-context screens, Yarn key and pointer event bridges, and the unobfuscated graphics extractor screen. The catalog selects the adapters for each target. 1.16.5 now uses the standard Yarn hooks. |
| Configuration | Version 1 retains `hudEnabled` and adds optional per-feature settings. Existing profiles load without conversion. |
| Tests | The shared suite adds GUI navigation and search, scrolling and interaction, the mapped closing gesture, live settings, and persistence coverage. |

## Build verification

With the project's existing JDK 25 and Gradle dependencies available, run `./gradlew --offline --console=plain --parallel --max-workers=4 --continue build` from the project root. Gradle compiles, tests, packages, and checks the jar for each target in [the version catalog](../gradle/minecraft-targets.json). The dependency pins and lockfiles are unchanged.

The full offline build passed for all 40 targets on JDK 25. Each target passed 27 JUnit cases, totaling 1,080 with zero failures, errors, or skips. All 40 production jars passed the package checks and contained the native screen and shared GUI classes.

## Runtime verification

Packaged Fabric profiles ran in local single-player worlds on Linux using Xvfb and software graphics. Minecraft 1.16.5 used Java 8 and 26.3 used Java 25. Both exercised Right Shift opening and closing, holding the opening key, F8 during play and inside the screen, search, selection, wheel scrolling, slider dragging, setting edits, and resize. The legacy profile reopened with text shadow off, line spacing 21, and version color #F9CEE9. Its Controls screen showed both bindings, and the ClickGUI opened after remapping it to F9. The 26.3 profile reloaded its disabled HUD and subsequently saved edits from every control type.

Runtime checks caught differences in SDL button numbering and Yarn shortcut character delivery. The corrected jars were tested again. The other 38 targets received build and automated test coverage. Multiplayer, other operating systems, and hardware graphics backends were outside the runtime checks.

To reproduce the game checks, install `averion-client-<minecraftId>.jar` and the pinned Fabric API in an isolated profile with the target's declared Java runtime. Join a local world and exercise Right Shift, a held opening press, feature selection, search, setting edits, mouse-wheel scrolling, window resize, and F8 with the screen open and closed. Quit normally and reopen the same profile to verify the values persisted. Confirm the two bindings appear under Averion Client in Controls.

## Scope

Phase 03 can reuse the canvas, native screen adapters, and configuration mutation path for a dedicated HUD Editor. Phase 02 covers the HUD's enable state and display settings. The screen uses mouse navigation, with keyboard entry and shortcuts for the search field.
