package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class FpsFeatureTest {
    @Test
    void countsFramesAndDiscardsTimeSpentOutsideTheGame() {
        FpsFeature fps = new FpsFeature();
        for (int frame = 0; frame <= 50; frame++) {
            fps.frame(1L + frame * 20_000_000L);
        }
        AtomicReference<String> label = new AtomicReference<>();
        HudText text = (value, x, y, color, shadow) -> label.set(value);
        fps.preview(text);
        assertEquals("50 FPS", label.get());
        fps.frame(9_000_000_000L);
        fps.preview(text);
        assertEquals("— FPS", label.get());
    }
}
