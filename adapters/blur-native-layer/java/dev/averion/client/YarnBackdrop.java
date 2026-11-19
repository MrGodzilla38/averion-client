package dev.averion.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

abstract class YarnBackdrop extends Screen {
    YarnBackdrop(Text title) {
        super(title);
    }

    final void paintBackdrop(float tickDelta) {
        // Minecraft submits the background stratum before calling Screen.render.
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        applyBlur(context);
    }
}
