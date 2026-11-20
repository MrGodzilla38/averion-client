package dev.averion.client;

/** Counts rendered frames over one-second windows on the render thread. */
final class FpsFeature extends HudElement {
    private final Setting.Color color = setting(new Setting.Color("color", "Text color", 0x92DBEF));
    private final Setting.Toggle shadow = setting(new Setting.Toggle("shadow", "Text shadow", true));
    private long start;
    private long previous;
    private int frames;
    private String label = "— FPS";

    FpsFeature() {
        super("fps", "FPS Counter", "Shows the current frame rate.", 8, 58);
    }

    void frame(long now) {
        if (previous == 0 || now - previous > 2_000_000_000L) {
            start = now;
            frames = 0;
            label = "— FPS";
        } else {
            frames++;
            long elapsed = now - start;
            if (elapsed >= 1_000_000_000L) {
                label = Math.round(frames * 1_000_000_000.0 / elapsed) + " FPS";
                frames = 0;
                start = now;
            }
        }
        previous = now;
    }

    @Override
    void drawContent(HudText text) {
        text.draw(label, 0, 0, 0xFF000000 | color.value(), shadow.value());
    }
}
