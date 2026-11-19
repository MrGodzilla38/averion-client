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
import java.util.UUID;

final class SettingsStore {
    private static final String FILE_NAME = "averion-client.json";
    private static final String SCHEMA_FIELD = "schemaVersion";
    private static final String HUD_FIELD = "hudEnabled";
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

    boolean isWritable() {
        return writable;
    }

    // Called by the client at startup, then exclusively by its single config writer.
    void saveHudEnabled(boolean enabled) throws IOException {
        if (!writable) {
            throw new IOException("Averion configuration is read-only");
        }
        document.addProperty(SCHEMA_FIELD, SCHEMA_VERSION);
        document.addProperty(HUD_FIELD, enabled);
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
