package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SettingsStoreTest {
    @TempDir
    Path dir;

    @Test
    void createsAndReloadsDefaultsInNewProfile() throws IOException {
        Path profileConfig = dir.resolve("config");
        SettingsStore store = new SettingsStore(profileConfig);
        assertTrue(store.loadHudEnabled());
        assertTrue(store.isWritable());
        assertTrue(new SettingsStore(profileConfig).loadHudEnabled());
        assertTrue(read(profileConfig.resolve("averion-client.json")).contains("\"schemaVersion\": 1"));
    }

    @Test
    void persistsToggleAndPreservesUnknownFields() throws IOException {
        Files.write(config(), "{\"schemaVersion\":1,\"extra\":{\"answer\":42}}".getBytes(StandardCharsets.UTF_8));
        SettingsStore store = new SettingsStore(dir);
        assertTrue(store.loadHudEnabled());
        store.saveHudEnabled(false);

        assertFalse(new SettingsStore(dir).loadHudEnabled());
        assertTrue(read(config()).contains("\"answer\": 42"));
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "[]", "{schemaVersion:1}", "{\"schemaVersion\":1}{}",
            "{\"schemaVersion\":1,\"hudEnabled\":\"false\"}", "{\"schemaVersion\":\"1\"}", "{\"hudEnabled\":false}"})
    void recoversInvalidConfigurationWithoutLosingOriginal(String invalid) throws IOException {
        Files.write(config(), invalid.getBytes(StandardCharsets.UTF_8));
        SettingsStore store = new SettingsStore(dir);
        assertTrue(store.loadHudEnabled());
        assertTrue(store.isWritable());
        assertTrue(read(config()).contains("\"schemaVersion\": 1"));
        try (Stream<Path> files = Files.list(dir)) {
            Path backup = files.filter(path -> path.getFileName().toString().startsWith("averion-client.invalid-"))
                    .findFirst().orElseThrow(AssertionError::new);
            assertEquals(invalid, read(backup));
        }
        store.saveHudEnabled(false);
        assertFalse(new SettingsStore(dir).loadHudEnabled());
    }

    @Test
    void preservesNewSchemaAndBlocksWrites() throws IOException {
        String original = "{\"schemaVersion\":2,\"hudEnabled\":false,\"future\":true}";
        Files.write(config(), original.getBytes(StandardCharsets.UTF_8));
        SettingsStore store = new SettingsStore(dir);
        assertTrue(store.loadHudEnabled());
        assertFalse(store.isWritable());
        assertThrows(IOException.class, () -> store.saveHudEnabled(false));
        assertEquals(original, read(config()));
    }

    @Test
    void unreadablePathUsesSessionDefaults() throws IOException {
        Files.createDirectory(config());
        SettingsStore store = new SettingsStore(dir);
        assertTrue(store.loadHudEnabled());
        assertFalse(store.isWritable());
        assertTrue(Files.isDirectory(config()));
    }

    @Test
    void loadsLegacyHudFlagValidatesNewSettingsAndKeepsNestedUnknownFields() throws IOException {
        String config = "{\"schemaVersion\":1,\"hudEnabled\":false,\"features\":{\"future\":{\"enabled\":true},"
                + "\"hud\":{\"settings\":{\"shadow\":\"invalid\",\"content\":\"UNKNOWN\","
                + "\"line_spacing\":200,\"version_color\":-1,\"extra\":42}}}}";
        Files.write(config(), config.getBytes(StandardCharsets.UTF_8));
        HudFeature hud = new HudFeature("26.3");
        Feature other = new Feature("other", "Other", "Other", Feature.Category.CLIENT, true) { };
        List<Feature> features = Arrays.asList(hud, other);
        SettingsStore store = new SettingsStore(dir);
        store.loadFeatures(features);
        assertFalse(hud.isEnabled());
        assertEquals("BOTH", hud.settings().get(0).encode().getAsString());
        assertEquals(true, hud.settings().get(1).value());
        assertEquals(22, hud.settings().get(2).value());
        assertEquals(0xB7CEE9, hud.settings().get(3).value());
        other.setEnabled(false);
        store.saveFeatures(SettingsStore.snapshot(features));

        other.setEnabled(true);
        new SettingsStore(dir).loadFeatures(features);
        assertFalse(other.isEnabled());
        assertTrue(read(config()).contains("\"extra\": 42"));
        assertTrue(read(config()).contains("\"future\""));
        assertTrue(read(config()).contains("\"hudEnabled\": false"));
    }

    private Path config() {
        return dir.resolve("averion-client.json");
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
