package dev.averion.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class ClientHooks implements HudCanvas {
    private final ClientRuntime runtime;
    private GuiGraphicsExtractor graphics;

    private ClientHooks(ClientRuntime runtime) {
        this.runtime = runtime;
    }

    static void register(ClientRuntime runtime) {
        ClientHooks hooks = new ClientHooks(runtime);
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("averion_client", "controls"));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(KeyBindings.TOGGLE_HUD, InputConstants.KEY_F8, category));
        runtime.keys().register(KeyBindings.TOGGLE_HUD, toggle::isDown, toggle::consumeClick, runtime::toggleHud);
        KeyMapping clickGui = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(KeyBindings.TOGGLE_GUI, InputConstants.KEY_RSHIFT, category));
        runtime.keys().register(KeyBindings.TOGGLE_GUI, clickGui::isDown, clickGui::consumeClick,
                () -> openGui(runtime, clickGui, clickGui));
        KeyMapping hudEditor = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(KeyBindings.OPEN_HUD_EDITOR, InputConstants.UNKNOWN.getValue(), category));
        runtime.keys().register(KeyBindings.OPEN_HUD_EDITOR, hudEditor::isDown, hudEditor::consumeClick,
                () -> openGui(runtime, clickGui, hudEditor));
        ClientTickEvents.END_CLIENT_TICK.register(client -> runtime.tick(playing(client) && client.isWindowActive()));
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR, Identifier.fromNamespaceAndPath("averion_client", HudFeature.ID), hooks::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> runtime.close());
    }

    private static void openGui(ClientRuntime runtime, KeyMapping clickGui, KeyMapping mapping) {
        Minecraft client = Minecraft.getInstance();
        if (mapping.isUnbound()) {
            return;
        }
        for (KeyMapping other : client.options.keyMappings) {
            if (other != mapping && mapping.same(other)) {
                return;
            }
        }
        TargetScreen.show(client, new TargetGui(runtime, client, clickGui, mapping != clickGui));
    }

    private static boolean playing(Minecraft client) {
        return client.level != null && client.player != null && TargetScreen.isClear(client);
    }

    private void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (playing(client) && !client.getDebugOverlay().showDebugScreen()) {
            graphics = extractor;
            try {
                runtime.render(this, extractor.guiWidth(), extractor.guiHeight());
            } finally {
                graphics = null;
            }
        }
    }

    @Override
    public void draw(String text, int x, int y, int color, boolean shadow) {
        graphics.text(Minecraft.getInstance().font, text, x, y, color, shadow);
    }

    @Override
    public int textWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    @Override
    public int lineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
        drawScaled(graphics, text, x, y, color, shadow, scale);
    }

    static void drawScaled(GuiGraphicsExtractor graphics, String text, float x, float y, int color, boolean shadow, float scale) {
        var pose = graphics.pose();
        pose.pushMatrix();
        try {
            pose.translate(x, y);
            pose.scale(scale, scale);
            graphics.text(Minecraft.getInstance().font, text, 0, 0, color, shadow);
        } finally {
            pose.popMatrix();
        }
    }
}
