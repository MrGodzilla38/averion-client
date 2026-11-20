package dev.averion.client;

import net.minecraft.client.util.math.MatrixStack;

final class TargetGui extends YarnGuiInput implements GuiCanvas, HudCanvas {
    private MatrixStack stack;

    TargetGui(ClientRuntime runtime, boolean held, TargetKeys keys, boolean editHud) {
        super(runtime, held, keys::matchesKey, keys::matchesMouse);
        if (editHud) {
            input.openHudEditor();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        stack = matrices;
        try {
            if (!input.editingHud()) {
                paintBackdrop(tickDelta);
            }
            input.render(this, this, mouseX, mouseY);
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

    @Override
    public int lineHeight() {
        return textRenderer.fontHeight;
    }

    @Override
    public void draw(String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            textRenderer.drawWithShadow(stack, text, x, y, color);
        } else {
            textRenderer.draw(stack, text, x, y, color);
        }
    }

    @Override
    public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
        TargetHud.drawScaled(stack, text, x, y, color, shadow, scale);
    }
}
