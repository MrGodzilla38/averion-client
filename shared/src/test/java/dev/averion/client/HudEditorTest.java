package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HudEditorTest {
    @TempDir
    Path dir;

    @Test
    void editorDragsAndScalesFpsIndependentlyAndReloadsSavedLayout() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            runtime.toggle(runtime.hudElements().get(1));
            GuiInput input = input(runtime);
            Canvas canvas = new Canvas();
            assertFalse(input.mouseClicked(270, 220, 0, false));
            assertFalse(input.editingHud());
            assertTrue(input.mouseClicked(50, 100, 0, false));
            input.render(canvas, canvas, 0, 0);
            assertTrue(input.mouseClicked(9, 59, 0, false));
            assertTrue(input.drag(141, 91, 0));
            assertTrue(input.mouseReleased(0, false));
            input.render(canvas, canvas, 0, 0);
            canvas.assertLine("— FPS", 140, 90, 1f);
            input.mouseClicked(280, 208, 0, false); // Scale up
            input.render(canvas, canvas, 0, 0);
            canvas.assertLine("— FPS", 140, 90, 1.1f);
            input.mouseClicked(270, 190, 0, false); // Save
            assertFalse(input.editingHud());
            assertTrue(runtime.hudElements().get(1).isEnabled());
            canvas.lines.clear();
            runtime.render(canvas, 320, 240);
            canvas.assertLine("Averion Client", 8, 8, 1f);
            canvas.assertLine("— FPS", 140, 90, 1.1f);
        }
        try (ClientRuntime restarted = new ClientRuntime(dir, "26.3")) {
            restarted.toggleHud();
            Canvas canvas = new Canvas();
            restarted.render(canvas, 320, 240);
            assertFalse(canvas.lines.containsKey("Averion Client"));
            canvas.assertLine("— FPS", 140, 90, 1.1f);
        }
    }

    @Test
    void editorSkipsDisabledElementsAndSavesDeletionOfTheLastVisibleElement() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            GuiInput input = input(runtime);
            Canvas canvas = new Canvas();
            input.openHudEditor();
            input.render(canvas, canvas, 0, 0);
            assertFalse(canvas.lines.containsKey("— FPS"));
            input.mouseClicked(9, 59, 0, false);
            assertFalse(input.drag(141, 91, 0));
            input.mouseClicked(40, 190, 0, false); // Cycle past disabled FPS
            input.mouseClicked(160, 190, 0, false); // Delete branding
            assertTrue(runtime.hudElements().get(0).isEnabled());

            canvas.lines.clear();
            canvas.labels.clear();
            input.render(canvas, canvas, 0, 0);
            assertTrue(canvas.lines.isEmpty());
            assertTrue(canvas.labels.contains("No active HUD elements"));
            assertFalse(canvas.labels.contains("Delete"));
            input.mouseClicked(270, 190, 0, false);
            assertFalse(input.editingHud());
            assertFalse(runtime.hudElements().get(0).isEnabled());
        }
        try (ClientRuntime restarted = new ClientRuntime(dir, "26.3")) {
            assertFalse(restarted.hudElements().get(0).isEnabled());
            GuiInput input = input(restarted);
            input.openHudEditor();
            Canvas canvas = new Canvas();
            input.render(canvas, canvas, 0, 0);
            assertTrue(canvas.labels.contains("No active HUD elements"));
        }
    }

    @Test
    void deletingSelectionKeepsTheNextEnabledElementEditable() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            runtime.toggle(runtime.hudElements().get(1));
            GuiInput input = input(runtime);
            input.openHudEditor();
            Canvas canvas = new Canvas();
            input.render(canvas, canvas, 0, 0);
            input.mouseClicked(160, 190, 0, false);
            canvas.lines.clear();
            canvas.labels.clear();
            input.render(canvas, canvas, 0, 0);
            assertFalse(canvas.lines.containsKey("Averion Client"));
            assertTrue(canvas.labels.contains("FPS Counter >"));
            assertTrue(canvas.labels.contains("Delete"));
            input.mouseClicked(9, 9, 0, false);
            assertFalse(input.drag(50, 50, 0));
            input.mouseClicked(9, 59, 0, false);
            assertTrue(input.drag(50, 80, 0));
        }
    }

    @Test
    void escapeDiscardsDraftAndMappedCloseKeepsItsReleaseGate() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            int[] closes = {0};
            GuiInput input = new GuiInput(runtime, true, () -> closes[0]++, () -> true, () -> "");
            input.resize(320, 240);
            input.mouseClicked(50, 100, 0, false);
            input.mouseClicked(160, 190, 0, false);
            assertTrue(input.keyPressed(GuiInput.Command.ESCAPE, false));
            assertFalse(input.editingHud());
            assertTrue(runtime.hudElements().get(0).isEnabled());
            assertEquals(0, closes[0]);

            input.openHudEditor();
            assertTrue(input.keyReleased(true));
            assertEquals(0, closes[0]);
            assertTrue(input.keyPressed(GuiInput.Command.NONE, true));
            assertTrue(input.keyReleased(true));
            assertEquals(1, closes[0]);
        }
    }

    @Test
    void anchorsFollowViewportSizeAndClampWithoutChangingStoredOffsets() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            GuiInput input = input(runtime);
            Canvas canvas = new Canvas();
            input.mouseClicked(50, 100, 0, false);
            input.render(canvas, canvas, 0, 0);
            input.mouseClicked(20, 208, 0, false);
            input.mouseClicked(20, 208, 0, false); // Right anchor
            input.mouseClicked(90, 208, 0, false); // Center Y
            input.mouseClicked(270, 190, 0, false);
            runtime.render(canvas, 320, 240);
            canvas.assertLine("Minecraft 26.3", 297, 120, 1f);
            runtime.render(canvas, 45, 15);
            canvas.assertLine("Minecraft 26.3", 22, 11, 1f);
            runtime.render(canvas, 640, 480);
            canvas.assertLine("Minecraft 26.3", 617, 240, 1f);
        }
    }

    @Test
    void legacyPositionsAndValidLayoutFieldsSurvivePartialOrUnknownConfig() throws Exception {
        Path file = dir.resolve("averion-client.json");
        Files.write(file, ("{\"schemaVersion\":1,\"hudEnabled\":true,\"features\":{"
                + "\"fps\":{\"enabled\":true,\"settings\":{\"shadow\":false},\"placement\":{"
                + "\"horizontal\":\"future\",\"x\":42,\"y\":10.5,\"scale\":500,\"future\":true}}}}")
                .getBytes(StandardCharsets.UTF_8));
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            Canvas canvas = new Canvas();
            runtime.render(canvas, 320, 240);
            canvas.assertLine("Averion Client", 8, 8, 1f);
            canvas.assertLine("— FPS", 42, 58, 1f);
            assertEquals(0f, canvas.lines.get("— FPS")[3]);
            runtime.toggleHud();
        }
        assertTrue(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).contains("\"future\": true"));
    }

    private static GuiInput input(ClientRuntime runtime) {
        GuiInput input = new GuiInput(runtime, false, () -> { }, () -> true, () -> "");
        input.resize(320, 240);
        return input;
    }

    private static final class Canvas implements GuiCanvas, HudCanvas {
        final Map<String, float[]> lines = new HashMap<>();
        final Set<String> labels = new HashSet<>();

        @Override
        public int textWidth(String text) {
            return text.length();
        }

        @Override
        public int lineHeight() {
            return 9;
        }

        @Override
        public void draw(String text, int x, int y, int color, boolean shadow) {
            drawScaled(text, x, y, color, shadow, 1f);
        }

        @Override
        public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
            lines.put(text, new float[]{x, y, scale, shadow ? 1 : 0});
        }

        @Override
        public void fill(int left, int top, int right, int bottom, int color) {
        }

        @Override
        public void text(String text, int x, int y, int color) {
            labels.add(text);
        }

        void assertLine(String label, float x, float y, float scale) {
            float[] line = lines.get(label);
            assertTrue(line != null, label);
            assertEquals(x, line[0], label);
            assertEquals(y, line[1], label);
            assertEquals(scale, line[2], label);
        }
    }
}
