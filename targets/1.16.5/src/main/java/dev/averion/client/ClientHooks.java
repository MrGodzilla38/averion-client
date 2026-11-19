package dev.averion.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

final class ClientHooks implements HudText {
    private final ClientRuntime runtime;
    private MatrixStack matrices;

    private ClientHooks(ClientRuntime runtime) {
        this.runtime = runtime;
    }

    static void register(ClientRuntime runtime) {
        ClientHooks hooks = new ClientHooks(runtime);
        KeyBinding toggle = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(KeyBindings.TOGGLE_HUD, GLFW.GLFW_KEY_F8, KeyBindings.CATEGORY));
        runtime.keys().register(KeyBindings.TOGGLE_HUD, toggle::isPressed, toggle::wasPressed, runtime::toggleHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> runtime.tick(playing(client) && client.isWindowFocused()));
        HudRenderCallback.EVENT.register(hooks::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> runtime.close());
    }

    private static boolean playing(MinecraftClient client) {
        return client.world != null && client.player != null && client.currentScreen == null
                && client.getOverlay() == null;
    }

    private void render(MatrixStack stack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (playing(client) && !client.options.hudHidden && !client.options.debugEnabled) {
            matrices = stack;
            try {
                runtime.render(this);
            } finally {
                matrices = null;
            }
        }
    }

    @Override
    public void draw(String text, int x, int y, int color) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, text, x, y, color);
    }
}
