package dev.averion.client;

import java.io.IOException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

final class LegacyBlur implements AutoCloseable {
    private final PostEffectProcessor effect;

    LegacyBlur(MinecraftClient client, int width, int height) throws IOException {
        effect = new PostEffectProcessor(client.getTextureManager(), client.getResourceManager(),
                client.getFramebuffer(), new Identifier("minecraft", "shaders/post/blur.json"));
        try {
            effect.setupDimensions(width, height);
        } catch (RuntimeException failure) {
            effect.close();
            throw failure;
        }
    }

    void render(float delta) {
        effect.render(delta);
    }

    @Override
    public void close() {
        effect.close();
    }
}
