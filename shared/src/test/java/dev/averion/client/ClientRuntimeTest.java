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
        runtime.render((text, x, y, color, shadow) -> lines.add(text));
        return lines;
    }
}
