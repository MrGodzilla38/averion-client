package dev.averion.client;

import org.lwjgl.glfw.GLFW;

final class YarnShortcuts {
    private YarnShortcuts() {
    }

    static boolean usesShortcutModifier(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_SUPER) != 0
                || (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) == GLFW.GLFW_MOD_CONTROL;
    }

    static GuiInput.Command command(int key, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            return GuiInput.Command.ESCAPE;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            return GuiInput.Command.BACKSPACE;
        }
        if (usesShortcutModifier(modifiers)) {
            switch (key) {
                case GLFW.GLFW_KEY_F:
                    return GuiInput.Command.FIND;
                case GLFW.GLFW_KEY_A:
                    return GuiInput.Command.SELECT_ALL;
                case GLFW.GLFW_KEY_V:
                    return GuiInput.Command.PASTE;
                default:
                    break;
            }
        }
        return GuiInput.Command.NONE;
    }
}
