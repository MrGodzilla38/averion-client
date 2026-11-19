package dev.averion.client;

import net.minecraft.client.Minecraft;

final class TargetScreen {
    private TargetScreen() {
    }

    static boolean isClear(Minecraft client) {
        return client.gui.screen() == null && client.gui.overlay() == null;
    }
}
