# Client configuration

Averion stores its settings in `averion-client.json` under Fabric Loader's per-profile configuration directory. A standard game profile uses `<game directory>/config/averion-client.json`. The file is UTF-8 JSON.

```json
{
  "schemaVersion": 1,
  "hudEnabled": true,
  "features": {
    "hud": {
      "settings": {
        "content": "BOTH",
        "shadow": true,
        "line_spacing": 11,
        "version_color": 12046057
      },
      "placement": {
        "horizontal": "start",
        "vertical": "start",
        "x": 8,
        "y": 8,
        "scale": 100
      }
    }
  }
}
```

`schemaVersion` is a positive integer. Phase 02 extends version 1 with the optional `features` object. The top-level `hudEnabled` boolean still controls the branding HUD and defaults to true. Phase 01 profiles retain that value and get defaults for the new settings. Other features can store `enabled` and `settings` under their stable feature ID. Minecraft stores the ClickGUI, F8 and optional HUD Editor assignments in the profile’s `options.txt`. The HUD Editor binding starts unassigned. F1 visibility and the debug overlay are live Minecraft settings.

The HUD’s `content` values are `BOTH`, `NAME`, and `VERSION`. `shadow` is boolean. `line_spacing` is a whole number from 9 to 22 GUI pixels and applies when two lines are visible. `version_color` is an unsigned 24-bit RGB integer. Missing or invalid feature settings use their defaults, and out-of-range line spacing is clamped.

The optional FPS counter stores its enable flag under `features.fps.enabled` and its RGB color and text shadow under `features.fps.settings` as `color` and `shadow`. It defaults to disabled.

## HUD placement

Phase 03 adds an optional `placement` object under each HUD element's feature ID. Schema 1 stores one active layout per game profile and loads it when the client starts.

| Field | Values |
| --- | --- |
| `horizontal` | `start` (left), `center` or `end` (right) |
| `vertical` | `start` (top), `center` or `end` (bottom) |
| `x`, `y` | Integer offsets in GUI pixels from the element's aligned position. Positive offsets move right or down. |
| `scale` | Integer percentage from 50 to 200. The editor adjusts scale in 10-point steps. |

Missing or invalid placement fields fall back individually to the element's defaults. Branding defaults to `(8, 8)` and FPS to `(8, 58)`, both aligned to the top left at 100%. The renderer limits the position to the available viewport when the element fits and keeps the stored offset for later resizes.

## Save and recovery

The runtime reads the file once at initialization. A toggle or setting edit changes the live feature immediately. **Save & back** applies all editor changes together. Both paths schedule a snapshot on one worker after a short delay. Quick changes replace pending writes, so the latest setting wins. A write stages a temporary file beside the configuration and atomically replaces the original. A failed write leaves the previous file in place and is logged. Normal shutdown waits up to three seconds for the writer.

Malformed JSON, invalid UTF-8 and invalid schema headers or `hudEnabled` values cause Averion to copy the original to `averion-client.invalid-<uuid>.json` and generate defaults. If the backup fails, Averion leaves the original in place and uses session-only defaults. It also uses session-only defaults for unsupported schema versions, unreadable files, nonregular paths or files above 64 KiB. Gameplay remains available and toggles still affect the current session. An in-game notice and the log indicate a session-only toggle or save failure.

Unknown root, feature, setting and placement keys in schema 1 survive normal writes. Future migrations should read and validate the old schema, preserve the original before any incompatible conversion, and write the new schema atomically after validation. A client that does not understand the new schema leaves it intact. Keep a single active game process per game directory when sharing configuration. Concurrent clients can overwrite each other's latest choice.

[Fabric Loader configuration directory](https://maven.fabricmc.net/docs/fabric-loader-0.20.1/net/fabricmc/loader/api/FabricLoader.html#getConfigDir%28%29).
