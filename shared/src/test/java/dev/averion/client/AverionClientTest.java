package dev.averion.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AverionClientTest {
    @ParameterizedTest
    @ValueSource(strings = {"1.16.5", "26.3"})
    void startupMessageIdentifiesClientBuildAndGameVersion(String minecraftVersion) {
        assertEquals("Averion Client 0.1.0-phase.0 initialized on Minecraft " + minecraftVersion,
                AverionClient.startupMessage("0.1.0-phase.0", minecraftVersion));
    }
}
