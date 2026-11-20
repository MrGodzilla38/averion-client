package dev.averion.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

final class TargetHud implements HudCanvas {
    private final ClientRuntime runtime;
    private final Predicate<MinecraftClient> playing;
    private MatrixStack context;

    private TargetHud(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        this.runtime = runtime;
        this.playing = playing;
    }

    static void register(ClientRuntime runtime, Predicate<MinecraftClient> playing) {
        TargetHud hud = new TargetHud(runtime, playing);
        HudRenderCallback.EVENT.register((context, ticks) -> hud.render(context));
    }

    private void render(MatrixStack current) {
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
        if (shadow) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(context, text, x, y, color);
        } else {
            MinecraftClient.getInstance().textRenderer.draw(context, text, x, y, color);
        }
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

    static void drawScaled(MatrixStack context, String text, float x, float y, int color, boolean shadow, float scale) {
        context.push();
        try {
            context.translate(x, y, 0);
            context.scale(scale, scale, 1);
            if (shadow) {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(context, text, 0, 0, color);
            } else {
                MinecraftClient.getInstance().textRenderer.draw(context, text, 0, 0, color);
            }
        } finally {
            context.pop();
        }
    }
}
