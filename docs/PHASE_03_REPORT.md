# Phase 03 report

## Completed features

- The branding HUD and FPS counter have independent position, horizontal and vertical alignment, 50–200% scale, visibility, and existing content or style settings.
- Open the HUD Editor with the **HUD Editor** button in ClickGUI’s left sidebar. Its optional native keybind starts unassigned and opens the editor directly from gameplay once assigned. The editor previews enabled HUD elements over the world. **Delete** removes the selected element from the draft and disables it on save. It can be re-enabled from its ClickGUI module card. Drag to position an element or use its alignment and scale controls. **Save & back** applies the whole draft. **Esc** discards it.
- Each profile stores one active layout in the existing schema 1 config and restores it on launch. Older files keep their enabled states and style settings and use the original positions at 100% scale. Unknown placement keys survive subsequent writes.
- The shared runtime uses the same measured bounds for gameplay and editor hit testing. A registered `HudElement` supplies local content and automatically participates in layout, preview and persistence. Target adapters supply the native font metrics, drawing and input. Gameplay rendering reuses its measurement state and skips hidden elements. The editor uses the existing configuration writer.
- ClickGUI retains its design and offers an editor button in the left sidebar. Its current settings, mapped open and close behavior, and F8 branding toggle remain available. Minecraft still owns binding persistence.

[User controls and extension points](FEATURE_SYSTEM.md), [Configuration format](CONFIGURATION.md), [Architecture](ARCHITECTURE.md).

## Build and automated tests

Run this command with JDK 25 and the project's Gradle cache populated.

```sh
./gradlew --offline --console=plain --parallel --max-workers=4 --continue build
```

The full offline build passed on Corretto JDK 25.0.5.1 for all 40 catalog targets. Each target passed 35 JUnit cases, for 1,400 cases with zero failures, errors or skips. All 40 production jars passed `verifyClientJar` and contained the shared HUD Editor class.

New regression tests cover shared gameplay and editor coordinates, FPS movement and scaling, filtering disabled elements, deleting the active selection, alignment across viewport resizes, draft cancellation and the mapped close gesture, profile reload, and compatibility with incomplete layout data and unknown config fields.

The 26.3 client compiler disables the `classfile` lint category for the game's JOML 1.10.9 matrix classes, which contain type-annotation attributes in legacy classfiles. The existing dependency pins and lockfiles are used.

## Real gameplay tests

Production jars and their pinned Fabric API bundles ran in isolated single-player profiles on Linux using Xvfb and Mesa software OpenGL. The profiles started with schema 1 settings from before Phase 03.

| Target | Observed in game |
| --- | --- |
| 1.16.5, Java 8 | The existing F9 remap opened and closed ClickGUI. The editor moved the FPS counter to `(260, 150)`, scaled it to 120%, and enabled it. The branding HUD kept its original placement and saved shadow setting. Esc discarded a draft. After quitting the game and reopening the same profile, the scaled FPS counter appeared in the saved position. The initial ClickGUI editor button opened the editor with the default key assignment left blank. |
| 26.3, Java 25 | Right Shift opened and closed ClickGUI. The editor placed the branding HUD at the right edge and vertical center at 130%, and moved the FPS counter independently to `(200, 120)`. Resizing the window from 900 × 600 to 760 × 520 repositioned the anchored branding while leaving the FPS offset in place. Both elements kept their saved layout after a full quit and relaunch. Key Binds showed the editor as Not Bound on first launch. Assigning F7 opened it directly from gameplay. The initial ClickGUI editor button opened it as well, and Ctrl+H left ClickGUI open. |

Follow-up runs on 26.3 confirmed that Right Shift opens ClickGUI and the editor opens from the left sidebar below the HUD category. The editor omitted the disabled FPS counter. Deleting the last enabled element showed the empty state. Esc discarded the deletion. Saving disabled the element, and re-enabling it from its module card restored it in the editor.

F8 toggled the branding separately from the FPS counter during gameplay and left the saved state alone inside the editor. On 26.3, F1 hid the vanilla HUD and both Averion elements.

To reproduce, install the relevant production jar and catalog-pinned Fabric API in an isolated profile. Join a local world, open ClickGUI and start the HUD Editor. Move and scale enabled elements, delete one from the draft, save, and return to gameplay. Resize the window with an element anchored to an edge, exercise F8 and F1, then quit normally and reopen the same profile.

## Unverified

The other 38 targets have build, automated test and production jar coverage. Gameplay verification covered the two versions named above. Multiplayer, other operating systems and graphics backends, and performance measurements are outside the verified scope.
