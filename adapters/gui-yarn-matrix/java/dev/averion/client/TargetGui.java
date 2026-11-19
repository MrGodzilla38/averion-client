package dev.averion.client;

import net.minecraft.client.util.math.MatrixStack;

final class TargetGui extends YarnGuiInput implements GuiCanvas {
    private MatrixStack stack;

    TargetGui(ClientRuntime runtime, boolean held, TargetKeys keys) {
        super(runtime, held, keys::matchesKey, keys::matchesMouse);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        stack = matrices;
        try {
            paintBackdrop(tickDelta);
            input.gui.render(this, mouseX, mouseY);
        } finally {
            stack = null;
        }
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int color) {
        fill(stack, left, top, right, bottom, color);
    }

    @Override
    public void text(String text, int x, int y, int color) {
        textRenderer.draw(stack, text, x, y, color);
    }

    @Override
    public int textWidth(String text) {
        return textRenderer.getWidth(text);
    }
}
