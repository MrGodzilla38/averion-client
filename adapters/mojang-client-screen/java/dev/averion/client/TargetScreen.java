package dev.averion.client;

import net.minecraft.client.Minecraft;

final class TargetScreen {
    private TargetScreen() {
    }

    static boolean isClear(Minecraft client) {
        return client.screen == null && client.getOverlay() == null;
    }
}
