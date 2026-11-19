package dev.averion.client;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

final class Notifications {
    enum Type {
        INFO(0xFFB7CEE9),
        WARNING(0xFFFFD284);

        private final int color;

        Type(int color) {
            this.color = color;
        }
    }

    private final LongSupplier nanoTime;
    private Notice active;

    Notifications(LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
    }

    void show(String title, String description, Type type, int durationMillis) {
        if (durationMillis <= 0) {
            throw new IllegalArgumentException("Notification duration must be positive");
        }
        long expiry = nanoTime.getAsLong() + TimeUnit.MILLISECONDS.toNanos(durationMillis);
        active = new Notice(Objects.requireNonNull(title), description, Objects.requireNonNull(type), expiry);
    }

    void render(HudText text) {
        Notice notice = active;
        if (notice == null) {
            return;
        }
        if (nanoTime.getAsLong() - notice.expiry >= 0) {
            clear();
            return;
        }
        text.draw(notice.title, 8, 34, notice.type.color);
        if (notice.description != null) {
            text.draw(notice.description, 8, 45, 0xFFF2F5FC);
        }
    }

    void clear() {
        active = null;
    }

    private static final class Notice {
        private final String title;
        private final String description;
        private final Type type;
        private final long expiry;

        private Notice(String title, String description, Type type, long expiry) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.expiry = expiry;
        }
    }
}
