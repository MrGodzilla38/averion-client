package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GuiInputTest {
    @TempDir
    Path dir;

    @Test
    void ignoresTheOpeningHoldAndClosesOnceOnTheNextRelease() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            AtomicInteger closes = new AtomicInteger();
            GuiInput input = new GuiInput(runtime, true, closes::incrementAndGet, () -> true, () -> "");
            assertTrue(input.keyPressed(GuiInput.Command.NONE, true));
            assertTrue(input.keyPressed(GuiInput.Command.NONE, true));
            assertTrue(input.keyReleased(true));
            assertEquals(0, closes.get());
            assertTrue(input.keyPressed(GuiInput.Command.NONE, true));
            assertTrue(input.keyPressed(GuiInput.Command.NONE, true));
            assertEquals(0, closes.get());
            assertTrue(input.keyReleased(true));
            assertTrue(input.keyReleased(true));
            assertEquals(1, closes.get());
        }
    }

    @Test
    void clearsPendingCloseOnFocusLossAndAcceptsRemappedMouseBinding() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            AtomicBoolean focused = new AtomicBoolean(true);
            AtomicInteger closes = new AtomicInteger();
            GuiInput input = new GuiInput(runtime, false, closes::incrementAndGet, focused::get, () -> "");
            assertFalse(input.keyReleased(false));
            assertTrue(input.mouseClicked(1, 1, 3, true));
            focused.set(false);
            input.tick();
            focused.set(true);
            assertTrue(input.mouseReleased(3, true));
            assertEquals(0, closes.get());
            assertTrue(input.mouseClicked(1, 1, 3, true));
            assertTrue(input.mouseReleased(3, true));
            assertEquals(1, closes.get());
        }
    }
}
