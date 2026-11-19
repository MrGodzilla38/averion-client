package dev.averion.client;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
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
    private final FpsFeature fps;
    private final ScheduledThreadPoolExecutor writer = new ScheduledThreadPoolExecutor(1, task -> {
        Thread worker = new Thread(task, "Averion config writer");
        worker.setDaemon(true);
        return worker;
    });
    private final AtomicBoolean saveWarning = new AtomicBoolean();
    private volatile boolean saveFailed;
    private ScheduledFuture<?> pendingSave;
    private boolean closed;

    ClientRuntime(Path configDirectory, String minecraftVersion) {
        settings = new SettingsStore(configDirectory);
        hud = features.register(new HudFeature(minecraftVersion));
        fps = features.register(new FpsFeature());
        settings.loadFeatures(features.all());
        writer.setRemoveOnCancelPolicy(true);
    }

    KeyBindings keys() {
        return keys;
    }

    List<Feature> features() {
        return features.all();
    }

    void toggleHud() {
        if (toggle(hud)) {
            String title = hud.isEnabled() ? "Averion HUD enabled" : "Averion HUD disabled";
            boolean writable = settings.isWritable();
            notifications.show(title, writable ? null : "Session only: settings are read-only.",
                    writable ? Notifications.Type.INFO : Notifications.Type.WARNING, NOTICE_DURATION_MILLIS);
        }
    }

    boolean toggle(Feature feature) {
        if (closed) {
            return false;
        }
        requireRegistered(feature);
        feature.setEnabled(!feature.isEnabled());
        requestSave();
        return true;
    }

    <T> void update(Feature feature, Setting<T> setting, T value) {
        if (closed) {
            return;
        }
        requireRegistered(feature);
        if (!feature.owns(setting)) {
            throw new IllegalArgumentException("Unregistered setting: " + setting.id);
        }
        if (setting.set(value)) {
            requestSave();
        }
    }

    String configurationStatus() {
        if (!settings.isWritable()) {
            return "Session only · settings are read-only";
        }
        return saveFailed ? "Settings could not be saved" : "";
    }

    private void requireRegistered(Feature feature) {
        if (features.get(feature.id) != feature) {
            throw new IllegalArgumentException("Unregistered feature: " + feature.id);
        }
    }

    private void requestSave() {
        if (settings.isWritable()) {
            if (pendingSave != null) {
                pendingSave.cancel(false);
            }
            JsonObject snapshot = SettingsStore.snapshot(features.all());
            pendingSave = writer.schedule(() -> save(snapshot), SAVE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    void tick(boolean playing) {
        if (!playing) {
            notifications.clear();
        }
        keys.tick(playing);
        if (playing && saveWarning.getAndSet(false)) {
            notifications.show("Averion settings could not be saved", "See the game log for details.",
                    Notifications.Type.WARNING, NOTICE_DURATION_MILLIS);
        }
    }

    void recordFrame() {
        fps.frame(System.nanoTime());
    }

    void render(HudText text) {
        if (!closed) {
            recordFrame();
            hud.render(text);
            if (fps.isEnabled()) {
                fps.preview((line, x, y, color, shadow) -> text.draw(line, x, y + 50, color, shadow));
            }
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

    private void save(JsonObject snapshot) {
        try {
            settings.saveFeatures(snapshot);
            saveFailed = false;
            saveWarning.set(false);
        } catch (IOException failure) {
            saveFailed = true;
            saveWarning.set(true);
            AverionClient.LOGGER.warn("Could not persist Averion feature settings", failure);
        }
    }
}
