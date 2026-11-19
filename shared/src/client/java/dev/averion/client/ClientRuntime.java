package dev.averion.client;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class ClientRuntime implements AutoCloseable {
    private static final int NOTICE_DURATION_MILLIS = 2_500;
    private static final int SAVE_DELAY_MILLIS = 150;

    private final SettingsStore settings;
    private final FeatureRegistry features = new FeatureRegistry();
    private final KeyBindings keys = new KeyBindings();
    private final Notifications notifications = new Notifications(System::nanoTime);
    private final HudFeature hud;
    private final ScheduledThreadPoolExecutor writer = new ScheduledThreadPoolExecutor(1, task -> {
        Thread worker = new Thread(task, "Averion config writer");
        worker.setDaemon(true);
        return worker;
    });
    private final AtomicBoolean saveFailed = new AtomicBoolean();
    private ScheduledFuture<?> pendingSave;
    private boolean closed;

    ClientRuntime(Path configDirectory, String minecraftVersion) {
        settings = new SettingsStore(configDirectory);
        hud = features.register(new HudFeature(minecraftVersion));
        hud.setEnabled(settings.loadHudEnabled());
        writer.setRemoveOnCancelPolicy(true);
    }

    KeyBindings keys() {
        return keys;
    }

    void toggleHud() {
        if (closed) {
            return;
        }
        boolean enabled = !hud.isEnabled();
        hud.setEnabled(enabled);
        String title = enabled ? "Averion HUD enabled" : "Averion HUD disabled";
        if (!settings.isWritable()) {
            notifications.show(title, "Session only: settings are read-only.", Notifications.Type.WARNING,
                    NOTICE_DURATION_MILLIS);
            return;
        }
        notifications.show(title, null, Notifications.Type.INFO, NOTICE_DURATION_MILLIS);
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        pendingSave = writer.schedule(() -> save(enabled), SAVE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    void tick(boolean playing) {
        if (!playing) {
            notifications.clear();
        }
        keys.tick(playing);
        if (playing && saveFailed.getAndSet(false)) {
            notifications.show("Averion settings could not be saved", "See the game log for details.",
                    Notifications.Type.WARNING, NOTICE_DURATION_MILLIS);
        }
    }

    void render(HudText text) {
        if (!closed) {
            hud.render(text);
            notifications.render(text);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        keys.clear();
        notifications.clear();
        features.close();
        writer.shutdown();
        try {
            if (!writer.awaitTermination(3, TimeUnit.SECONDS)) {
                writer.shutdownNow();
                AverionClient.LOGGER.warn("Timed out waiting for Averion configuration writes");
            }
        } catch (InterruptedException interrupted) {
            writer.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void save(boolean enabled) {
        try {
            settings.saveHudEnabled(enabled);
            saveFailed.set(false);
        } catch (IOException failure) {
            saveFailed.set(true);
            AverionClient.LOGGER.warn("Could not persist Averion HUD setting", failure);
        }
    }
}
