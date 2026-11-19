package dev.averion.client;

import net.minecraft.client.gui.DrawContext;

final class TargetGui extends YarnGuiInput implements GuiCanvas {
    private DrawContext context;

    TargetGui(ClientRuntime runtime, boolean held, TargetKeys keys) {
        super(runtime, held, keys::matchesKey, keys::matchesMouse);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        context = drawContext;
        try {
            paintBackdrop(tickDelta);
            input.gui.render(this, mouseX, mouseY);
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
}
