package dev.averion.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

final class YarnUi {
    private YarnUi() {
    }

    static void show(Screen screen) {
        MinecraftClient.getInstance().openScreen(screen);
    }

    static Object[] keyBindings() {
        return MinecraftClient.getInstance().options.keysAll;
    }
}
