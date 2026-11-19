package dev.averion.client;

final class HudFeature extends Feature {
    static final String ID = "hud";
    private static final int WHITE = 0xFFF2F5FC;
    private static final int SOFT_BLUE = 0xFFB7CEE9;

    private final String versionLine;

    HudFeature(String minecraftVersion) {
        super(ID, "Averion Client", "Shows the active Minecraft version during play.", Category.HUD, false);
        versionLine = "Minecraft " + minecraftVersion;
    }

    void render(HudText text) {
        if (isEnabled()) {
            text.draw(name, 8, 8, WHITE);
            text.draw(versionLine, 8, 19, SOFT_BLUE);
        }
    }
}
