package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClickGuiTest {
    @TempDir
    Path dir;

    @Test
    void searchesRegisteredFeaturesAndNavigatesBackToTheCategory() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            ClickGui gui = new ClickGui(runtime);
            gui.resize(640, 360);
            assertTrue(frame(gui).contains("HUD"));
            assertFalse(frame(gui).contains("Render"));
            assertTrue(gui.click(470, 30, 0));
            gui.insert("render");
            assertTrue(frame(gui).contains("No matches"));
            assertFalse(frame(gui).contains("Averion Client"));
            gui.selectQuery();
            gui.insert("MINECRAFT");
            assertTrue(frame(gui).contains("Search results"));
            assertTrue(frame(gui).contains("Averion Client"));
            assertTrue(gui.click(40, 74, 0));
            assertTrue(frame(gui).contains("Features"));
            assertTrue(gui.click(160, 138, 0));
            assertTrue(frame(gui).contains("Feature enabled"));
        }
    }

    @Test
    void scrollsWholeRowsAfterResizeAndAdjustsExpandedColorChannels() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            ClickGui gui = new ClickGui(runtime);
            gui.resize(320, 180);
            assertTrue(gui.click(110, 70, 0));
            assertFalse(frame(gui).contains("Version color"));
            assertTrue(gui.scroll(240, 80, -0.5));
            assertTrue(gui.scroll(240, 80, -0.5));
            assertTrue(frame(gui).contains("Line spacing"));
            assertFalse(frame(gui).contains("Version color"));
            assertTrue(gui.scroll(240, 80, -1));
            assertTrue(frame(gui).contains("Version color"));
            assertTrue(gui.click(240, 128, 0));
            assertTrue(frame(gui).contains("Red"));
            assertTrue(gui.scroll(240, 80, -2));
            assertTrue(frame(gui).contains("Blue"));
            assertTrue(gui.click(215, 77, 0));
            assertTrue(gui.drag(400, 0));
            assertTrue(gui.release(0));
            assertFalse(gui.drag(215, 0));

            gui.resize(640, 360);
            assertTrue(frame(gui).contains("#FFCEE9"));
            assertTrue(frame(gui).contains("Feature enabled"));
            assertFalse(gui.scroll(430, 80, -1));
        }
    }

    @Test
    void editsLiveHudAndPersistsChangesTogetherAcrossRestart() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            ClickGui gui = new ClickGui(runtime);
            gui.resize(640, 360);
            assertTrue(gui.click(160, 138, 0));
            assertTrue(gui.click(470, 74, 0));
            assertFalse(runtime.features().get(0).isEnabled());
            assertTrue(gui.click(470, 74, 0));
            assertTrue(gui.click(470, 104, 0));
            assertEquals(1, hudLines(runtime).size());
            assertTrue(gui.click(470, 104, 1));
            assertTrue(gui.click(470, 134, 0));
            assertTrue(gui.click(470, 174, 0));
            assertTrue(gui.drag(650, 0));
            assertTrue(gui.release(0));
            assertTrue(gui.click(470, 194, 0));
            assertTrue(gui.click(600, 234, 0));
            assertTrue(gui.drag(650, 0));
            assertTrue(gui.release(0));
            assertEquals("Minecraft 26.3 @30 ffffcee9 false", hudLines(runtime).get(1));
            assertTrue(gui.click(470, 104, 1));
            assertEquals("Minecraft 26.3 @8 ffffcee9 false", hudLines(runtime).get(0));
            assertTrue(gui.click(470, 74, 0));
            runtime.tick(false);
        }
        try (ClientRuntime restarted = new ClientRuntime(dir, "26.3")) {
            assertFalse(restarted.features().get(0).isEnabled());
            assertTrue(hudLines(restarted).isEmpty());
            restarted.toggleHud();
            assertEquals("Minecraft 26.3 @8 ffffcee9 false", hudLines(restarted).get(0));
            ClickGui gui = new ClickGui(restarted);
            gui.resize(640, 360);
            assertTrue(gui.click(160, 138, 0));
            assertTrue(gui.click(470, 104, 0));
            assertEquals("Minecraft 26.3 @30 ffffcee9 false", hudLines(restarted).get(1));
        }
    }

    @Test
    void togglesFpsFromTheCardAndSavesItsOwnSettings() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            ClickGui gui = new ClickGui(runtime);
            gui.resize(640, 360);
            assertFalse(frame(gui).contains("Feature enabled"));
            assertTrue(gui.click(445, 171, 0));
            assertTrue(runtime.features().get(1).isEnabled());
            assertFalse(frame(gui).contains("Feature enabled"));
            assertTrue(gui.click(340, 150, 0));
            assertTrue(frame(gui).contains("Text color"));
            assertTrue(gui.click(480, 140, 0));
        }
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            assertTrue(hudLines(runtime).get(2).endsWith(" false"));
        }
    }

    private static List<String> hudLines(ClientRuntime runtime) {
        List<String> output = new ArrayList<>();
        runtime.render(ClientRuntimeTest.canvas((text, x, y, color, shadow) ->
                output.add(text + " @" + y + " " + Integer.toHexString(color) + " " + shadow)), 640, 360);
        return output;
    }

    private static List<String> frame(ClickGui gui) {
        List<String> output = new ArrayList<>();
        gui.render(new GuiCanvas() {
            @Override
            public void fill(int left, int top, int right, int bottom, int color) {
            }

            @Override
            public void text(String text, int x, int y, int color) {
                output.add(text);
            }

            @Override
            public int textWidth(String text) {
                return text.length() * 6;
            }
        }, 0, 0);
        return output;
    }
}
