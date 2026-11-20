package dev.averion.client;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import net.minecraft.client.MinecraftClient;

abstract class YarnGuiInput extends YarnBackdrop {
    final GuiInput input;
    private final BiPredicate<Integer, Integer> keyMatches;
    private final IntPredicate mouseMatches;

    YarnGuiInput(ClientRuntime runtime, boolean held, BiPredicate<Integer, Integer> keyMatches,
                 IntPredicate mouseMatches) {
        super(YarnGuiTitle.title());
        this.keyMatches = keyMatches;
        this.mouseMatches = mouseMatches;
        MinecraftClient game = MinecraftClient.getInstance();
        input = new GuiInput(runtime, held, () -> YarnUi.show(null), game::isWindowFocused, game.keyboard::getClipboard);
    }

    @Override
    protected void init() {
        input.resize(width, height);
    }

    @Override
    public void tick() {
        input.tick();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return input.keyPressed(YarnShortcuts.command(key, modifiers), keyMatches.test(key, scanCode))
                || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        return input.keyReleased(keyMatches.test(key, scanCode));
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return !YarnShortcuts.usesShortcutModifier(modifiers) && input.typeLegacy(character);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return input.mouseClicked(x, y, button, mouseMatches.test(button));
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return input.mouseReleased(button, mouseMatches.test(button));
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return input.drag(x, y, button);
    }

    // Yarn changed the scroll signature in 1.20.2.
    public boolean mouseScrolled(double x, double y, double vertical) {
        return input.scroll(x, y, vertical);
    }

    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return input.scroll(x, y, vertical);
    }
}
