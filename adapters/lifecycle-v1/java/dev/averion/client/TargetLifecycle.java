package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

final class TargetLifecycle {
    private TargetLifecycle() {
    }

    static void register(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> runtime.tick(playing.test(client) && client.isWindowFocused()));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> runtime.close());
    }
}
