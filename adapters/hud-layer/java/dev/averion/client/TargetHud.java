package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

final class TargetHud implements HudText {
    private final ClientRuntime runtime;
    private final Predicate<MinecraftClient> playing;
    private DrawContext context;

    private TargetHud(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        this.runtime = runtime;
        this.playing = playing;
    }

    static void register(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        TargetHud hud = new TargetHud(runtime, playing);
        HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR,
                Identifier.of("averion_client", HudFeature.ID), (context, ticks) -> hud.render(context));
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
    public void draw(String text, int x, int y, int color, boolean shadow) {
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, shadow);
    }
}
