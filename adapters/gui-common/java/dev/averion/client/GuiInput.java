package dev.averion.client;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

final class GuiInput {
    enum Command {
        ESCAPE, FIND, BACKSPACE, SELECT_ALL, PASTE, NONE
    }

    final ClickGui gui;
    private final Runnable close;
    private final BooleanSupplier focused;
    private final Supplier<String> clipboard;
    private boolean openingHeld;
    private boolean closePressed;
    private char highSurrogate;

    GuiInput(ClientRuntime runtime, boolean openingHeld, Runnable close,
             BooleanSupplier focused, Supplier<String> clipboard) {
        gui = new ClickGui(runtime);
        this.openingHeld = openingHeld;
        this.close = close;
        this.focused = focused;
        this.clipboard = clipboard;
    }

    boolean keyPressed(Command command, boolean matchesBinding) {
        if (matchesBinding) {
            closePressed = !openingHeld && focused.getAsBoolean();
            return true;
        }
        if (command == Command.ESCAPE) {
            close.run();
            return true;
        }
        if (command == Command.FIND) {
            gui.focusSearch();
            return true;
        }
        if (!gui.searchFocused()) {
            return false;
        }
        if (command == Command.BACKSPACE) {
            gui.backspace();
        } else if (command == Command.SELECT_ALL) {
            gui.selectQuery();
        } else if (command == Command.PASTE) {
            gui.insert(clipboard.get());
        }
        return true;
    }

    boolean keyReleased(boolean matchesBinding) {
        if (!matchesBinding) {
            return false;
        }
        // Consume the opening release and close only after a fresh, complete press.
        boolean shouldClose = closePressed && focused.getAsBoolean();
        closePressed = false;
        openingHeld = false;
        if (shouldClose) {
            close.run();
        }
        return true;
    }

    boolean typeLegacy(char character) {
        if (Character.isHighSurrogate(character)) {
            highSurrogate = character;
            return gui.searchFocused();
        }
        char pending = highSurrogate;
        highSurrogate = 0;
        if (Character.isLowSurrogate(character)) {
            return pending != 0 && gui.type(Character.toCodePoint(pending, character));
        }
        return gui.type(character);
    }

    boolean mouseClicked(double x, double y, int button, boolean matchesBinding) {
        if (matchesBinding) {
            closePressed = !openingHeld && focused.getAsBoolean();
            return true;
        }
        if (gui.closeHit(x, y, button)) {
            close.run();
            return true;
        }
        return gui.click(x, y, button);
    }

    boolean mouseReleased(int button, boolean matchesBinding) {
        return keyReleased(matchesBinding) || gui.release(button);
    }

    void tick() {
        if (!focused.getAsBoolean()) {
            openingHeld = false;
            closePressed = false;
            highSurrogate = 0;
            gui.release(0);
        }
    }
}
