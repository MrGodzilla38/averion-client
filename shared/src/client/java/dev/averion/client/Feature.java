package dev.averion.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

abstract class Feature {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_]*");

    enum Category {
        HUD("HUD"), RENDER("Render"), PERFORMANCE("Performance"),
        MOVEMENT("Movement"), MISC("Misc"), CLIENT("Client");

        final String label;

        Category(String label) {
            this.label = label;
        }
    }

    final String id;
    final String name;
    final String description;
    final Category category;
    private final Map<String, Setting<?>> settings = new LinkedHashMap<>();
    private boolean enabled;

    Feature(String id, String name, String description, Category category, boolean enabled) {
        this.id = requireId(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.category = Objects.requireNonNull(category);
        this.enabled = enabled;
    }

    static String requireId(String id) {
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
        return id;
    }

    final <S extends Setting<?>> S setting(S setting) {
        if (settings.putIfAbsent(setting.id, setting) != null) {
            throw new IllegalArgumentException("Duplicate setting ID: " + setting.id);
        }
        return setting;
    }

    final boolean owns(Setting<?> setting) {
        return settings.get(setting.id) == setting;
    }

    final List<Setting<?>> settings() {
        return Collections.unmodifiableList(new ArrayList<>(settings.values()));
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

    void preview(HudText text) {
        text.draw(name, 8, 8, 0xFFF0F5FA, false);
    }

    void onEnabledChanged(boolean enabled) {
        // Features can acquire or release their resources on state transitions.
    }
}
