package dev.averion.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.Function;

abstract class Setting<T> {
    final String id;
    final String label;
    private final T defaultValue;
    private T value;

    Setting(String id, String label, T defaultValue) {
        this.id = Feature.requireId(id);
        this.label = label;
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    final T value() {
        return value;
    }

    final boolean set(T next) {
        T normalized = normalize(next);
        if (value.equals(normalized)) {
            return false;
        }
        value = normalized;
        return true;
    }

    final void load(JsonElement stored) {
        T decoded = stored != null && stored.isJsonPrimitive() ? decode(stored.getAsJsonPrimitive()) : null;
        value = decoded == null ? defaultValue : normalize(decoded);
    }

    T normalize(T next) {
        return next;
    }

    abstract T decode(JsonPrimitive stored);

    abstract JsonPrimitive encode();

    private static Integer integer(JsonPrimitive stored) {
        if (stored.isNumber()) {
            try {
                return Integer.valueOf(stored.getAsString());
            } catch (NumberFormatException ignored) {
                // A fractional or out-of-range representation is invalid for an integer setting.
            }
        }
        return null;
    }

    static final class Toggle extends Setting<Boolean> {
        Toggle(String id, String label, boolean initial) {
            super(id, label, initial);
        }

        @Override
        Boolean decode(JsonPrimitive stored) {
            return stored.isBoolean() ? stored.getAsBoolean() : null;
        }

        @Override
        JsonPrimitive encode() {
            return new JsonPrimitive(value());
        }
    }

    static final class Slider extends Setting<Integer> {
        final int min;
        final int max;

        Slider(String id, String label, int initial, int min, int max) {
            super(id, label, initial);
            if (min >= max || initial < min || initial > max) {
                throw new IllegalArgumentException("Invalid slider bounds for " + id);
            }
            this.min = min;
            this.max = max;
        }

        @Override
        Integer normalize(Integer next) {
            return Math.max(min, Math.min(next, max));
        }

        @Override
        Integer decode(JsonPrimitive stored) {
            return integer(stored);
        }

        @Override
        JsonPrimitive encode() {
            return new JsonPrimitive(value());
        }
    }

    static final class Choice<E extends Enum<E>> extends Setting<E> {
        private final E[] choices;
        private final Function<E, String> labelOf;

        Choice(String id, String label, E initial, Function<E, String> labelOf) {
            super(id, label, initial);
            choices = initial.getDeclaringClass().getEnumConstants();
            this.labelOf = labelOf;
        }

        String selectedLabel() {
            return labelOf.apply(value());
        }

        E next(int direction) {
            return choices[Math.floorMod(value().ordinal() + direction, choices.length)];
        }

        @Override
        E decode(JsonPrimitive stored) {
            if (stored.isString()) {
                for (E choice : choices) {
                    if (choice.name().equals(stored.getAsString())) {
                        return choice;
                    }
                }
            }
            return null;
        }

        @Override
        JsonPrimitive encode() {
            return new JsonPrimitive(value().name());
        }
    }

    static final class Color extends Setting<Integer> {
        Color(String id, String label, int initial) {
            super(id, label, initial);
            if (initial < 0 || initial > 0xFFFFFF) {
                throw new IllegalArgumentException("Expected an RGB color for " + id);
            }
        }

        @Override
        Integer normalize(Integer next) {
            return next & 0xFFFFFF;
        }

        @Override
        Integer decode(JsonPrimitive stored) {
            Integer rgb = integer(stored);
            return rgb != null && rgb >= 0 && rgb <= 0xFFFFFF ? rgb : null;
        }

        @Override
        JsonPrimitive encode() {
            return new JsonPrimitive(value());
        }
    }
}
