package dev.averion.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class TargetGui extends Screen implements GuiCanvas {
    private final KeyMapping mapping;
    private final GuiInput input;
    private GuiGraphicsExtractor graphics;

    TargetGui(ClientRuntime runtime, Minecraft game, KeyMapping mapping) {
        super(Component.literal("Averion"));
        this.mapping = mapping;
        input = new GuiInput(runtime, mapping.isDown(), () -> TargetScreen.show(game, null),
                game::isWindowActive, game.keyboardHandler::getClipboard);
    }

    @Override
    protected void init() {
        input.gui.resize(width, height);
    }

    @Override
    public void tick() {
        input.tick();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        extractBlurredBackground(extractor);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float tickDelta) {
        graphics = extractor;
        try {
            input.gui.render(this, mouseX, mouseY);
        } finally {
            graphics = null;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent key) {
        return input.keyPressed(command(key), mapping.matches(key)) || super.keyPressed(key);
    }

    private static GuiInput.Command command(KeyEvent key) {
        if (key.isEscape()) {
            return GuiInput.Command.ESCAPE;
        }
        if (key.key() == InputConstants.KEY_BACKSPACE) {
            return GuiInput.Command.BACKSPACE;
        }
        if (key.isSelectAll()) {
            return GuiInput.Command.SELECT_ALL;
        }
        if (key.isPaste()) {
            return GuiInput.Command.PASTE;
        }
        if (key.key() == InputConstants.KEY_F && key.hasControlDownWithQuirk()) {
            return GuiInput.Command.FIND;
        }
        return GuiInput.Command.NONE;
    }

    @Override
    public boolean keyReleased(KeyEvent key) {
        return input.keyReleased(mapping.matches(key));
    }

    @Override
    public boolean charTyped(CharacterEvent character) {
        return input.gui.type(character.codepoint());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent button, boolean doubleClick) {
        return input.mouseClicked(button.x(), button.y(), guiButton(button), mapping.matchesMouse(button));
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent button) {
        return input.mouseReleased(guiButton(button), mapping.matchesMouse(button));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent button, double dx, double dy) {
        return input.gui.drag(button.x(), guiButton(button));
    }

    private static int guiButton(MouseButtonEvent button) {
        // The game's SDL backend changed button numbering in 26.3.
        switch (button.button()) {
            case InputConstants.MOUSE_BUTTON_LEFT:
                return 0;
            case InputConstants.MOUSE_BUTTON_RIGHT:
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return input.gui.scroll(x, y, vertical);
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, bottom, color);
    }

    @Override
    public void text(String text, int x, int y, int color) {
        graphics.text(font, text, x, y, color, false);
    }

    @Override
    public int textWidth(String text) {
        return font.width(text);
    }
}
