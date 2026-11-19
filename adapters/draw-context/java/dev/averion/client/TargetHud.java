package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

final class TargetHud implements HudText {
    private final ClientRuntime runtime;
    private final Predicate<MinecraftClient> playing;
    private DrawContext context;

    private TargetHud(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        this.runtime = runtime;
        this.playing = playing;
    }

    // This is the published HUD callback on versions before the layered registry.
    @SuppressWarnings("deprecation")
    static void register(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        TargetHud hud = new TargetHud(runtime, playing);
        HudRenderCallback.EVENT.register((context, ticks) -> hud.render(context));
    }

    private void render(DrawContext current) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (playing.test(client) && !client.options.hudHidden && !TargetDebug.visible(client)) {
            context = current;
            try {
                runtime.render(this);
            } finally {
                context = null;
            }
        }
    }

    @Override
    public void draw(String text, int x, int y, int color) {
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x, y, color);
    }
}
