package dev.averion.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Locale;

final class HudPlacement {
    static final int MIN_SCALE = 50;
    static final int MAX_SCALE = 200;

    enum Anchor {
        START, CENTER, END;

        private static final Anchor[] VALUES = values();

        int origin(int available) {
            return ordinal() * available / 2;
        }

        int margin() {
            return 8 - ordinal() * 8;
        }

        Anchor next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    final Anchor horizontal;
    final Anchor vertical;
    final int x;
    final int y;
    final int scale;

    HudPlacement(int x, int y) {
        this(Anchor.START, Anchor.START, x, y, 100);
    }

    HudPlacement(Anchor horizontal, Anchor vertical, int x, int y, int scale) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    static HudPlacement load(JsonObject values, HudPlacement defaults) {
        return new HudPlacement(
                anchor(values.get("horizontal"), defaults.horizontal),
                anchor(values.get("vertical"), defaults.vertical),
                integer(values.get("x"), defaults.x, -1_000_000, 1_000_000),
                integer(values.get("y"), defaults.y, -1_000_000, 1_000_000),
                integer(values.get("scale"), defaults.scale, MIN_SCALE, MAX_SCALE));
    }

    JsonObject encode() {
        JsonObject values = new JsonObject();
        values.addProperty("horizontal", horizontal.key());
        values.addProperty("vertical", vertical.key());
        values.addProperty("x", x);
        values.addProperty("y", y);
        values.addProperty("scale", scale);
        return values;
    }

    private static Anchor anchor(JsonElement value, Anchor fallback) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            for (Anchor anchor : Anchor.VALUES) {
                if (anchor.key().equals(value.getAsString())) {
                    return anchor;
                }
            }
        }
        return fallback;
    }

    private static int integer(JsonElement value, int fallback, int min, int max) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            try {
                int number = Integer.parseInt(value.getAsString());
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException invalid) {
                return fallback;
            }
        }
        return fallback;
    }
}
