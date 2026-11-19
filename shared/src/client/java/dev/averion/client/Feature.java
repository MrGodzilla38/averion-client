package dev.averion.client;

import java.util.Objects;
import java.util.regex.Pattern;

abstract class Feature {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_]*");

    enum Category {
        HUD
    }

    final String id;
    final String name;
    final String description;
    final Category category;
    private boolean enabled;

    Feature(String id, String name, String description, Category category, boolean enabled) {
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid feature ID: " + id);
        }
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.category = Objects.requireNonNull(category);
        this.enabled = enabled;
    }

    final boolean isEnabled() {
        return enabled;
    }

    final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            onEnabledChanged(enabled);
            this.enabled = enabled;
        }
    }

    void onEnabledChanged(boolean enabled) {
        // Features can acquire or release their resources on state transitions.
    }
}
