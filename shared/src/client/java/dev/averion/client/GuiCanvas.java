package dev.averion.client;

/** Native GUI coordinates and font metrics for the current render pass. */
interface GuiCanvas {
    void fill(int left, int top, int right, int bottom, int color);

    void text(String text, int x, int y, int color);

    int textWidth(String text);

    default void text(String value, int x, int y, int color, int maxWidth) {
        if (maxWidth <= 0) {
            return;
        }
        if (textWidth(value) <= maxWidth) {
            text(value, x, y, color);
            return;
        }
        String suffix = "…";
        if (textWidth(suffix) > maxWidth) {
            return;
        }
        int end = value.length();
        do {
            end = value.offsetByCodePoints(end, -1);
        } while (textWidth(value.substring(0, end) + suffix) > maxWidth);
        text(value.substring(0, end) + suffix, x, y, color);
    }
}
