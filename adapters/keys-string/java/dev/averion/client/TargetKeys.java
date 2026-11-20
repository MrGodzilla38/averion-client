package dev.averion.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

final class TargetKeys {
    private final KeyBinding gui;

    private TargetKeys(KeyBinding gui) {
        this.gui = gui;
    }

    static void register(ClientRuntime runtime) {
        KeyBinding toggle = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(KeyBindings.TOGGLE_HUD, GLFW.GLFW_KEY_F8, KeyBindings.CATEGORY));
        runtime.keys().register(KeyBindings.TOGGLE_HUD, toggle::isPressed, toggle::wasPressed, runtime::toggleHud);
        KeyBinding clickGui = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(KeyBindings.TOGGLE_GUI, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBindings.CATEGORY));
        TargetKeys keys = new TargetKeys(clickGui);
        runtime.keys().register(KeyBindings.TOGGLE_GUI, clickGui::isPressed, clickGui::wasPressed,
                () -> keys.open(runtime, clickGui));
        KeyBinding hudEditor = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(KeyBindings.OPEN_HUD_EDITOR, GLFW.GLFW_KEY_UNKNOWN, KeyBindings.CATEGORY));
        runtime.keys().register(KeyBindings.OPEN_HUD_EDITOR, hudEditor::isPressed, hudEditor::wasPressed,
                () -> keys.open(runtime, hudEditor));
    }

    private void open(ClientRuntime runtime, KeyBinding mapping) {
        if (mapping.isUnbound()) {
            return;
        }
        for (Object binding : YarnUi.keyBindings()) {
            KeyBinding other = (KeyBinding) binding;
            if (other != mapping && mapping.equals(other)) {
                return;
            }
        }
        YarnUi.show(new TargetGui(runtime, gui.isPressed(), this, mapping != gui));
    }

    boolean matchesKey(int key, int scanCode) {
        return gui.matchesKey(key, scanCode);
    }

    boolean matchesMouse(int button) {
        return gui.matchesMouse(button);
    }
}
