package dev.averion.client;

import net.minecraft.client.gui.DrawContext;

final class TargetGui extends YarnGuiInput implements GuiCanvas, HudCanvas {
    private DrawContext context;

    TargetGui(ClientRuntime runtime, boolean held, TargetKeys keys, boolean editHud) {
        super(runtime, held, keys::matchesKey, keys::matchesMouse);
        if (editHud) {
            input.openHudEditor();
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        context = drawContext;
        try {
            if (!input.editingHud()) {
                paintBackdrop(tickDelta);
            }
            input.render(this, this, mouseX, mouseY);
        } finally {
            context = null;
        }
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int color) {
        context.fill(left, top, right, bottom, color);
    }

    @Override
    public void text(String text, int x, int y, int color) {
        context.drawText(textRenderer, text, x, y, color, false);
    }

    @Override
    public int textWidth(String text) {
        return textRenderer.getWidth(text);
    }

    @Override
    public int lineHeight() {
        return textRenderer.fontHeight;
    }

    @Override
    public void draw(String text, int x, int y, int color, boolean shadow) {
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    @Override
    public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
        TargetHud.drawScaled(context, text, x, y, color, shadow, scale);
    }
}
