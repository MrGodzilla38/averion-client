package dev.averion.client;

final class HudFeature extends HudElement {
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
                8, 8);
        versionLine = "Minecraft " + minecraftVersion;
    }

    @Override
    void drawContent(HudText text) {
        Content shown = content.value();
        if (shown != Content.VERSION) {
            text.draw(name, 0, 0, 0xFFF2F5FC, shadow.value());
        }
        if (shown != Content.NAME) {
            int y = shown == Content.VERSION ? 0 : spacing.value();
            text.draw(versionLine, 0, y, 0xFF000000 | versionColor.value(), shadow.value());
        }
    }
}
