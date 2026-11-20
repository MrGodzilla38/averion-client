package dev.averion.client;

/** Native font metrics and transformed drawing in scaled GUI coordinates. */
interface HudCanvas extends HudText {
    int textWidth(String text);

    int lineHeight();

    void drawScaled(String text, float x, float y, int color, boolean shadow, float scale);
}
