package dev.averion.client;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

final class GuiInput {
    enum Command {
        ESCAPE, FIND, BACKSPACE, SELECT_ALL, PASTE, NONE
    }

    final ClickGui gui;
    private final ClientRuntime runtime;
    private HudEditor editor;
    private int width;
    private int height;
    private final Runnable close;
    private final BooleanSupplier focused;
    private final Supplier<String> clipboard;
    private boolean openingHeld;
    private boolean closePressed;
    private char highSurrogate;

    GuiInput(ClientRuntime runtime, boolean openingHeld, Runnable close,
             BooleanSupplier focused, Supplier<String> clipboard) {
        this.runtime = runtime;
        gui = new ClickGui(runtime);
        this.openingHeld = openingHeld;
        this.close = close;
        this.focused = focused;
        this.clipboard = clipboard;
    }

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
        gui.resize(width, height);
        if (editor != null) {
            editor.resize(width, height);
        }
    }

    void openHudEditor() {
        gui.release(0);
        editor = new HudEditor(runtime, width, height);
    }

    boolean editingHud() {
        return editor != null;
    }

    void render(GuiCanvas guiCanvas, HudCanvas hudCanvas, int mouseX, int mouseY) {
        if (editor == null) {
            gui.render(guiCanvas, mouseX, mouseY);
        } else {
            editor.render(guiCanvas, hudCanvas);
        }
    }

    boolean keyPressed(Command command, boolean matchesBinding) {
        if (matchesBinding) {
            closePressed = !openingHeld && focused.getAsBoolean();
            return true;
        }
        if (editor != null) {
            if (command == Command.ESCAPE) {
                editor = null;
            }
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

    boolean type(int codePoint) {
        return editor == null && gui.type(codePoint);
    }

    boolean typeLegacy(char character) {
        if (editor != null) {
            return false;
        }
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
        if (editor != null) {
            if (button == 0 && editor.click(x, y)) {
                editor = null;
            }
            return true;
        }
        if (gui.closeHit(x, y, button)) {
            close.run();
            return true;
        }
        if (gui.hudEditorHit(x, y, button)) {
            openHudEditor();
            return true;
        }
        return gui.click(x, y, button);
    }

    boolean mouseReleased(int button, boolean matchesBinding) {
        if (keyReleased(matchesBinding)) {
            return true;
        }
        return editor == null ? gui.release(button) : button == 0 && editor.release();
    }

    boolean drag(double x, double y, int button) {
        return editor == null ? gui.drag(x, button) : button == 0 && editor.drag(x, y);
    }

    boolean scroll(double x, double y, double vertical) {
        return editor == null && gui.scroll(x, y, vertical);
    }

    void tick() {
        if (!focused.getAsBoolean()) {
            openingHeld = false;
            closePressed = false;
            highSurrogate = 0;
            gui.release(0);
            if (editor != null) {
                editor.release();
            }
        }
    }
}
