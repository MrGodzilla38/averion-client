package dev.averion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class TargetScreen {
    private TargetScreen() {
    }

    static boolean isClear(Minecraft client) {
        return client.gui.screen() == null && client.gui.overlay() == null;
    }

    static void show(Minecraft client, Screen screen) {
        client.gui.setScreen(screen);
    }
}
