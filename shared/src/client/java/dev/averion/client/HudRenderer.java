package dev.averion.client;

/** Reuses the same measured bounds for rendering and editor hit testing. */
final class HudRenderer {
    private final HudText measure = this::measureLine;
    private final HudText paint = this::paintLine;
    private HudCanvas canvas;
    private int contentWidth;
    private int contentHeight;
    private float scale;
    int left;
    int top;
    int width;
    int height;

    void measure(HudCanvas target, HudElement element, HudPlacement placement, int screenWidth, int screenHeight) {
        contentWidth = 0;
        contentHeight = 0;
        canvas = target;
        try {
            element.drawContent(measure);
        } finally {
            canvas = null;
        }
        scale = placement.scale / 100f;
        width = (contentWidth * placement.scale + 99) / 100;
        height = (contentHeight * placement.scale + 99) / 100;
        left = clamp(placement.horizontal.origin(screenWidth - width) + placement.x, screenWidth - width);
        top = clamp(placement.vertical.origin(screenHeight - height) + placement.y, screenHeight - height);
    }

    void draw(HudCanvas target, HudElement element) {
        canvas = target;
        try {
            element.drawContent(paint);
        } finally {
            canvas = null;
        }
    }

    boolean contains(double x, double y) {
        return x >= left - 2 && x < left + width + 2 && y >= top - 2 && y < top + height + 2;
    }

    static int clamp(int position, int available) {
        return Math.max(0, Math.min(position, available));
    }

    private void measureLine(String text, int x, int y, int color, boolean shadow) {
        int extra = shadow ? 1 : 0;
        contentWidth = Math.max(contentWidth, x + canvas.textWidth(text) + extra);
        contentHeight = Math.max(contentHeight, y + canvas.lineHeight() + extra);
    }

    private void paintLine(String text, int x, int y, int color, boolean shadow) {
        if (scale == 1f) {
            canvas.draw(text, left + x, top + y, color, shadow);
        } else {
            canvas.drawScaled(text, left + x * scale, top + y * scale, color, shadow, scale);
        }
    }
}
