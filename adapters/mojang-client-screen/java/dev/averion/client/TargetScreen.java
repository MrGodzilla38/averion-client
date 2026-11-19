package dev.averion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class TargetScreen {
    private TargetScreen() {
    }

    static boolean isClear(Minecraft client) {
        return client.screen == null && client.getOverlay() == null;
    }

    static void show(Minecraft client, Screen screen) {
        client.setScreen(screen);
    }
}
