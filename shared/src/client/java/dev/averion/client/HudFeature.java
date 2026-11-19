package dev.averion.client;

final class HudFeature extends Feature {
    static final String ID = "hud";

    private enum Content {
        BOTH("Name & version"), NAME("Name only"), VERSION("Version only");

        final String label;

        Content(String label) {
            this.label = label;
        }
    }

    private final Setting.Choice<Content> content = setting(
            new Setting.Choice<>("content", "Content", Content.BOTH, option -> option.label));
    private final Setting.Toggle shadow = setting(new Setting.Toggle("shadow", "Text shadow", true));
    private final Setting.Slider spacing = setting(new Setting.Slider("line_spacing", "Line spacing", 11, 9, 22));
    private final Setting.Color versionColor = setting(new Setting.Color("version_color", "Version color", 0xB7CEE9));
    private final String versionLine;

    HudFeature(String minecraftVersion) {
        super(ID, "Averion Client", "Shows Averion branding and the active Minecraft version during play.",
                Category.HUD, false);
        versionLine = "Minecraft " + minecraftVersion;
    }

    void render(HudText text) {
        if (!isEnabled()) {
            return;
        }
        preview(text);
    }

    @Override
    void preview(HudText text) {
        Content shown = content.value();
        if (shown != Content.VERSION) {
            text.draw(name, 8, 8, 0xFFF2F5FC, shadow.value());
        }
        if (shown != Content.NAME) {
            int y = shown == Content.VERSION ? 8 : 8 + spacing.value();
            text.draw(versionLine, 8, y, 0xFF000000 | versionColor.value(), shadow.value());
        }
    }
}
