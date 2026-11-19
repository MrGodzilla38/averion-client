package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeatureRegistryTest {
    @Test
    void registersByStableIdAndKeepsOriginalOnDuplicate() {
        try (FeatureRegistry registry = new FeatureRegistry()) {
            TrackedFeature actual = registry.register(new TrackedFeature("test_hud"));
            assertSame(actual, registry.get("test_hud"));
            assertThrows(IllegalArgumentException.class, () -> registry.register(new TrackedFeature("test_hud")));
            assertSame(actual, registry.get("test_hud"));
            assertThrows(IllegalArgumentException.class, () -> registry.get("missing"));
            assertThrows(IllegalArgumentException.class, () -> new TrackedFeature("Invalid ID"));
        }
    }

    @Test
    void stateTransitionsAndShutdownNotifyFeatureExactlyOnce() {
        FeatureRegistry registry = new FeatureRegistry();
        TrackedFeature feature = registry.register(new TrackedFeature("test_hud"));
        assertFalse(feature.isEnabled());
        feature.setEnabled(true);
        feature.setEnabled(true);
        assertTrue(feature.isEnabled());
        registry.close();
        registry.close();

        assertFalse(feature.isEnabled());
        assertEquals(Arrays.asList(true, false), feature.transitions);
        assertThrows(IllegalStateException.class, () -> registry.register(new TrackedFeature("another_hud")));
    }

    private static final class TrackedFeature extends Feature {
        private final List<Boolean> transitions = new ArrayList<>();

        private TrackedFeature(String id) {
            super(id, "Test HUD", "Tracks activation for this test.", Category.HUD, false);
        }

        @Override
        void onEnabledChanged(boolean enabled) {
            transitions.add(enabled);
        }
    }
}
