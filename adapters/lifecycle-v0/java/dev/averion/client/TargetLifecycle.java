package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public final class TargetLifecycle {
    private static ClientRuntime active;

    private TargetLifecycle() {
    }

    static void register(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        active = runtime;
        ClientTickCallback.EVENT.register(client -> runtime.tick(playing.test(client) && client.isWindowFocused()));
    }

    public static void stop() {
        if (active != null) {
            active.close();
            active = null;
        }
    }
}
