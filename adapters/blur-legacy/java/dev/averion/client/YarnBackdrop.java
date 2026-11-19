package dev.averion.client;

import java.io.IOException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

abstract class YarnBackdrop extends Screen {
    private LegacyBlur blur;
    private int pixelsWide = -1;
    private int pixelsHigh = -1;

    YarnBackdrop(Text title) {
        super(title);
    }

    final void paintBackdrop(float tickDelta) {
        MinecraftClient game = MinecraftClient.getInstance();
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (width != pixelsWide || height != pixelsHigh) {
            releaseBlur();
            pixelsWide = width;
            pixelsHigh = height;
            try {
                blur = new LegacyBlur(game, width, height);
            } catch (IOException failure) {
                AverionClient.LOGGER.warn("Could not load the Minecraft menu blur", failure);
            } finally {
                game.getFramebuffer().beginWrite(true);
            }
        }
        if (blur != null) {
            try {
                blur.render(tickDelta);
            } finally {
                game.getFramebuffer().beginWrite(true);
            }
        }
    }

    @Override
    public void removed() {
        releaseBlur();
        pixelsWide = -1;
        super.removed();
    }

    private void releaseBlur() {
        if (blur != null) {
            blur.close();
            blur = null;
        }
    }
}
