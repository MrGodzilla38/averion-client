package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

final class TargetHud implements HudCanvas {
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
                runtime.render(this, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            } finally {
                context = null;
            }
        }
    }

    @Override
    public void draw(String text, int x, int y, int color, boolean shadow) {
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, shadow);
    }

    @Override
    public int textWidth(String text) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    @Override
    public int lineHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    @Override
    public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
        drawScaled(context, text, x, y, color, shadow, scale);
    }

    static void drawScaled(DrawContext context, String text, float x, float y, int color, boolean shadow, float scale) {
        var matrices = context.getMatrices();
        matrices.push();
        try {
            matrices.translate(x, y, 0);
            matrices.scale(scale, scale, 1);
            context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, shadow);
        } finally {
            matrices.pop();
        }
    }
}
