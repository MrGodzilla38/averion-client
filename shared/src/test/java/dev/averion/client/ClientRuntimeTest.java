package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientRuntimeTest {
    @TempDir
    Path dir;

    @Test
    void toggleUpdatesHudAndFeedbackThenFlushesLastChoiceToNextSession() {
        try (ClientRuntime runtime = new ClientRuntime(dir, "26.3")) {
            assertEquals(Arrays.asList("Averion Client", "Minecraft 26.3"), render(runtime));
            runtime.toggleHud();
            assertEquals(Collections.singletonList("Averion HUD disabled"), render(runtime));

            runtime.toggleHud();
            assertEquals(Arrays.asList("Averion Client", "Minecraft 26.3", "Averion HUD enabled"), render(runtime));
            runtime.toggleHud();
            runtime.tick(false);
            assertTrue(render(runtime).isEmpty());
        }
        try (ClientRuntime restarted = new ClientRuntime(dir, "26.3")) {
            assertTrue(render(restarted).isEmpty());
        }
    }

    private static List<String> render(ClientRuntime runtime) {
        List<String> lines = new ArrayList<>();
        runtime.render(canvas((text, x, y, color, shadow) -> lines.add(text)), 320, 240);
        return lines;
    }

    static HudCanvas canvas(HudText capture) {
        return new HudCanvas() {
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
                capture.draw(text, x, y, color, shadow);
            }

            @Override
            public void drawScaled(String text, float x, float y, int color, boolean shadow, float scale) {
                capture.draw(text, Math.round(x), Math.round(y), color, shadow);
            }
        };
    }
}
