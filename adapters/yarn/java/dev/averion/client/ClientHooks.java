package dev.averion.client;

import net.minecraft.client.MinecraftClient;

final class ClientHooks {
    private ClientHooks() {
    }

    static void register(ClientRuntime runtime) {
        TargetKeys.register(runtime);
        TargetLifecycle.register(runtime, ClientHooks::playing);
        TargetHud.register(runtime, ClientHooks::playing);
    }

    private static boolean playing(MinecraftClient client) {
        return client.world != null && client.player != null && client.currentScreen == null
                && client.getOverlay() == null;
    }
}
