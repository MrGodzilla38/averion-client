package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class KeyBindingsTest {
    private final KeyBindings keys = new KeyBindings();
    private boolean down;
    private int clicks;
    private int toggles;

    @Test
    void dispatchesTapAndPressButCoalescesHeldRepeats() {
        register();
        down = true;
        clicks = 1;
        keys.tick(true);
        clicks = 5;
        keys.tick(true);
        assertEquals(1, toggles);
        assertEquals(0, clicks);

        down = false;
        keys.tick(true);
        clicks = 1;
        keys.tick(true);
        assertEquals(2, toggles);
    }

    @Test
    void drainsMenuClicksAndRejectsDuplicateRegistration() {
        register();
        assertThrows(IllegalArgumentException.class, this::register);
        down = true;
        clicks = 1;
        keys.tick(false);
        clicks = 1;
        keys.tick(true);
        assertEquals(0, toggles);

        down = false;
        keys.tick(true);
        clicks = 1;
        keys.tick(true);
        assertEquals(1, toggles);
    }

    private void register() {
        keys.register(KeyBindings.TOGGLE_HUD, () -> down, this::consume, () -> toggles++);
    }

    private boolean consume() {
        if (clicks == 0) {
            return false;
        }
        clicks--;
        return true;
    }
}
