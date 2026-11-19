package dev.averion.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

abstract class YarnBackdrop extends Screen {
    YarnBackdrop(Text title) {
        super(title);
    }

    final void paintBackdrop(float tickDelta) {
        applyBlur();
    }
}
