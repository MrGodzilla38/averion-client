package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class NotificationsTest {
    private final AtomicLong clock = new AtomicLong();
    private final Notifications notifications = new Notifications(clock::get);

    @Test
    void latestNoticeReplacesPreviousAndExpiresAtItsOwnDeadline() {
        notifications.show("Enabled", null, Notifications.Type.INFO, 100);
        assertEquals(Collections.singletonList("Enabled"), render());
        clock.set(TimeUnit.MILLISECONDS.toNanos(75));
        notifications.show("Could not save", "See log", Notifications.Type.WARNING, 100);
        clock.set(TimeUnit.MILLISECONDS.toNanos(174));
        assertEquals(Arrays.asList("Could not save", "See log"), render());
        clock.set(TimeUnit.MILLISECONDS.toNanos(175));
        assertTrue(render().isEmpty());
    }

    @Test
    void clearsOnLeavingGameplayAndRejectsInvalidDuration() {
        notifications.show("Enabled", null, Notifications.Type.INFO, 100);
        notifications.clear();
        assertTrue(render().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> notifications.show("Invalid", null, Notifications.Type.INFO, 0));
    }

    private List<String> render() {
        List<String> lines = new ArrayList<>();
        notifications.render((text, x, y, color, shadow) -> lines.add(text));
        return lines;
    }
}
