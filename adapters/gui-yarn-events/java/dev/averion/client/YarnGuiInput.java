package dev.averion.client;

import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

abstract class YarnGuiInput extends YarnBackdrop {
    final GuiInput input;
    private final Predicate<KeyInput> keyMatches;
    private final Predicate<Click> mouseMatches;

    YarnGuiInput(ClientRuntime runtime, boolean held, Predicate<KeyInput> keyMatches, Predicate<Click> mouseMatches) {
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
    public boolean keyPressed(KeyInput key) {
        return input.keyPressed(YarnShortcuts.command(key.key(), key.modifiers()), keyMatches.test(key)) || super.keyPressed(key);
    }

    @Override
    public boolean keyReleased(KeyInput key) {
        return input.keyReleased(keyMatches.test(key));
    }

    @Override
    public boolean charTyped(CharInput character) {
        return !YarnShortcuts.usesShortcutModifier(character.modifiers())
                && input.type(character.codepoint());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        return input.mouseClicked(click.x(), click.y(), click.button(), mouseMatches.test(click));
    }

    @Override
    public boolean mouseReleased(Click click) {
        return input.mouseReleased(click.button(), mouseMatches.test(click));
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        return input.drag(click.x(), click.y(), click.button());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return input.scroll(x, y, vertical);
    }
}
