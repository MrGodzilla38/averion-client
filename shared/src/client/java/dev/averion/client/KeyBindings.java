package dev.averion.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

final class KeyBindings {
    static final String TOGGLE_HUD = "key.averion_client.toggle_hud";
    static final String TOGGLE_GUI = "key.averion_client.toggle_gui";
    static final String OPEN_HUD_EDITOR = "key.averion_client.open_hud_editor";
    static final String CATEGORY = "key.category.averion_client.controls";

    private final List<Binding> bindings = new ArrayList<>();

    void register(String id, BooleanSupplier isDown, BooleanSupplier consumeClick, Runnable action) {
        if (bindings.stream().anyMatch(binding -> binding.id.equals(id))) {
            throw new IllegalArgumentException("Duplicate keybinding: " + id);
        }
        bindings.add(new Binding(id, isDown, consumeClick, action));
    }

    void tick(boolean canAct) {
        for (int index = 0; index < bindings.size(); index++) {
            Binding binding = bindings.get(index);
            boolean wasHeld = binding.held;
            binding.held = binding.isDown.getAsBoolean();
            boolean clicked = false;
            // Vanilla queues click counts. Drain repeats and clicks from disallowed contexts.
            while (binding.consumeClick.getAsBoolean()) {
                clicked = true;
            }
            if (canAct && clicked && !wasHeld) {
                binding.action.run();
            }
        }
    }

    void clear() {
        bindings.clear();
    }

    private static final class Binding {
        private final String id;
        private final BooleanSupplier isDown;
        private final BooleanSupplier consumeClick;
        private final Runnable action;
        private boolean held;

        private Binding(String id, BooleanSupplier isDown, BooleanSupplier consumeClick, Runnable action) {
            this.id = id;
            this.isDown = isDown;
            this.consumeClick = consumeClick;
            this.action = action;
        }
    }
}
