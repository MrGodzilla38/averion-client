package dev.averion.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class SettingsStore {
    private static final String FILE_NAME = "averion-client.json";
    private static final String SCHEMA_FIELD = "schemaVersion";
    private static final String HUD_FIELD = "hudEnabled";
    private static final String FEATURES_FIELD = "features";
    private static final String ENABLED_FIELD = "enabled";
    private static final String PLACEMENT_FIELD = "placement";
    private static final String SETTINGS_FIELD = "settings";
    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_BYTES = 64 * 1024;
    private static final TypeAdapter<JsonElement> JSON = new Gson().getAdapter(JsonElement.class);

    private final Path file;
    private JsonObject document = new JsonObject();
    private boolean writable = true;

    SettingsStore(Path configDirectory) {
        file = configDirectory.resolve(FILE_NAME);
    }

    boolean loadHudEnabled() {
        try {
            BasicFileAttributes attributes = Files.readAttributes(
                    file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile() || attributes.size() > MAX_BYTES) {
                return useReadOnlyDefaults("Expected a regular configuration file of at most " + MAX_BYTES + " bytes");
            }
            JsonObject settings = readObject();
            JsonElement schema = settings.get(SCHEMA_FIELD);
            if (schema == null || !schema.isJsonPrimitive() || !schema.getAsJsonPrimitive().isNumber()
                    || !schema.getAsString().matches("[1-9][0-9]*")) {
                return recoverInvalid("Invalid or missing schemaVersion");
            }
            if (!Integer.toString(SCHEMA_VERSION).equals(schema.getAsString())) {
                return useReadOnlyDefaults("Unsupported Averion configuration schema");
            }
            JsonElement hud = settings.get(HUD_FIELD);
            if (hud != null && (!hud.isJsonPrimitive() || !hud.getAsJsonPrimitive().isBoolean())) {
                return recoverInvalid("hudEnabled must be a boolean");
            }
            document = settings;
            return hud != null ? hud.getAsBoolean() : createDefaults();
        } catch (NoSuchFileException missing) {
            return createDefaults();
        } catch (MalformedJsonException | EOFException | CharacterCodingException | StackOverflowError invalid) {
            return recoverInvalid("Malformed JSON or invalid UTF-8");
        } catch (IOException unreadable) {
            AverionClient.LOGGER.warn("Could not read Averion configuration at " + file, unreadable);
            return useReadOnlyDefaults("Configuration could not be read");
        }
    }

    void loadFeatures(List<Feature> features) {
        boolean hudEnabled = loadHudEnabled();
        JsonObject modules = object(document, FEATURES_FIELD);
        for (Feature feature : features) {
            JsonObject module = object(modules, feature.id);
            JsonObject stored = object(module, SETTINGS_FIELD);
            for (Setting<?> setting : feature.settings()) {
                setting.load(stored.get(setting.id));
            }
            if (feature instanceof HudElement) {
                HudElement element = (HudElement) feature;
                element.placement = HudPlacement.load(object(module, PLACEMENT_FIELD), element.defaultPlacement);
            }
            JsonElement enabled = module.get(ENABLED_FIELD);
            if (feature.id.equals(HudFeature.ID)) {
                feature.setEnabled(hudEnabled);
            } else if (enabled != null && enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean()) {
                feature.setEnabled(enabled.getAsBoolean());
            }
        }
    }

    boolean isWritable() {
        return writable;
    }

    // Snapshot only the registered fields on the game thread. The writer owns the loaded JSON thereafter.
    static JsonObject snapshot(List<Feature> features) {
        JsonObject snapshot = new JsonObject();
        JsonObject modules = new JsonObject();
        for (Feature feature : features) {
            JsonObject module = new JsonObject();
            if (feature.id.equals(HudFeature.ID)) {
                snapshot.addProperty(HUD_FIELD, feature.isEnabled());
            } else {
                module.addProperty(ENABLED_FIELD, feature.isEnabled());
            }
            JsonObject values = new JsonObject();
            for (Setting<?> setting : feature.settings()) {
                values.add(setting.id, setting.encode());
            }
            module.add(SETTINGS_FIELD, values);
            if (feature instanceof HudElement) {
                module.add(PLACEMENT_FIELD, ((HudElement) feature).placement.encode());
            }
            modules.add(feature.id, module);
        }
        snapshot.add(FEATURES_FIELD, modules);
        return snapshot;
    }

    void saveHudEnabled(boolean enabled) throws IOException {
        JsonObject values = new JsonObject();
        values.addProperty(HUD_FIELD, enabled);
        saveFeatures(values);
    }

    void saveFeatures(JsonObject snapshot) throws IOException {
        if (!writable) {
            throw new IOException("Averion configuration is read-only");
        }
        document.addProperty(SCHEMA_FIELD, SCHEMA_VERSION);
        if (snapshot.has(HUD_FIELD)) {
            document.add(HUD_FIELD, snapshot.get(HUD_FIELD));
        }
        if (snapshot.has(FEATURES_FIELD)) {
            JsonObject modules = object(document, FEATURES_FIELD);
            for (Map.Entry<String, JsonElement> entry : object(snapshot, FEATURES_FIELD).entrySet()) {
                JsonObject module = object(modules, entry.getKey());
                JsonObject next = entry.getValue().getAsJsonObject();
                if (next.has(ENABLED_FIELD)) {
                    module.add(ENABLED_FIELD, next.get(ENABLED_FIELD));
                }
                mergeFields(module, next, SETTINGS_FIELD);
                if (next.has(PLACEMENT_FIELD)) {
                    mergeFields(module, next, PLACEMENT_FIELD);
                }
                modules.add(entry.getKey(), module);
            }
            document.add(FEATURES_FIELD, modules);
        }
        Path dir = file.getParent();
        Files.createDirectories(dir);
        Path staged = Files.createTempFile(dir, "averion-client-", ".tmp");
        try {
            try (BufferedWriter output = Files.newBufferedWriter(staged, StandardCharsets.UTF_8)) {
                JsonWriter writer = new JsonWriter(output);
                writer.setIndent("  ");
                JSON.write(writer, document);
                output.write('\n');
            }
            Files.move(staged, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(staged);
        }
    }

    private static void mergeFields(JsonObject module, JsonObject next, String field) {
        JsonObject values = object(module, field);
        for (Map.Entry<String, JsonElement> value : object(next, field).entrySet()) {
            values.add(value.getKey(), value.getValue());
        }
        module.add(field, values);
    }

    private static JsonObject object(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private JsonObject readObject() throws IOException {
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(file, StandardCharsets.UTF_8))) {
            // Using the adapter directly keeps the reader strict on legacy Gson as well.
            JsonElement root = JSON.read(reader);
            if (!root.isJsonObject() || reader.peek() != JsonToken.END_DOCUMENT) {
                throw new MalformedJsonException("Expected one JSON object");
            }
            return root.getAsJsonObject();
        }
    }

    private boolean recoverInvalid(String reason) {
        Path backup = file.resolveSibling("averion-client.invalid-" + UUID.randomUUID() + ".json");
        try {
            Files.copy(file, backup);
            AverionClient.LOGGER.warn("Saved invalid Averion configuration to {} ({})", backup, reason);
            return createDefaults();
        } catch (IOException backupFailure) {
            AverionClient.LOGGER.warn("Could not back up Averion configuration at " + file, backupFailure);
            return useReadOnlyDefaults(reason);
        }
    }

    private boolean createDefaults() {
        try {
            saveHudEnabled(true);
        } catch (IOException failure) {
            // A later toggle may succeed after a temporary filesystem problem clears.
            AverionClient.LOGGER.warn("Could not write default Averion configuration to " + file, failure);
        }
        return true;
    }

    private boolean useReadOnlyDefaults(String reason) {
        writable = false;
        AverionClient.LOGGER.warn("Using session-only Averion defaults for {} ({})", file, reason);
        return true;
    }
}
