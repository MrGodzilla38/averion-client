package dev.averion.client;

/** A draw target used only during the synchronous HUD callback. */
@FunctionalInterface
interface HudText {
    void draw(String text, int x, int y, int color);
}
