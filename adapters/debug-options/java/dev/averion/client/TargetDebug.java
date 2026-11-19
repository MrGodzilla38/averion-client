package dev.averion.client;

import net.minecraft.client.MinecraftClient;

final class TargetDebug {
    private TargetDebug() {
    }

    static boolean visible(MinecraftClient client) {
        return client.options.debugEnabled;
    }
}
