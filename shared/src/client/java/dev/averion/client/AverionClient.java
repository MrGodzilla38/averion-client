package dev.averion.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AverionClient implements ClientModInitializer {
    static final Logger LOGGER = LogManager.getLogger("Averion Client");

    private static ClientRuntime runtime;

    @Override
    public void onInitializeClient() {
        start();
    }

    private static synchronized void start() {
        if (runtime != null) {
            return;
        }
        FabricLoader loader = FabricLoader.getInstance();
        String clientVersion = versionOf(loader, "averion_client");
        String minecraftVersion = versionOf(loader, "minecraft");
        ClientRuntime client = new ClientRuntime(loader.getConfigDir(), minecraftVersion);
        try {
            ClientHooks.register(client);
        } catch (RuntimeException | LinkageError failure) {
            client.close();
            throw failure;
        }
        runtime = client;
        LOGGER.info(startupMessage(clientVersion, minecraftVersion));
    }

    static String startupMessage(String clientVersion, String minecraftVersion) {
        return "Averion Client " + clientVersion + " initialized on Minecraft " + minecraftVersion;
    }

    private static String versionOf(FabricLoader loader, String modId) {
        return loader.getModContainer(modId)
                .orElseThrow(() -> new IllegalStateException("Missing Fabric mod container for " + modId))
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}
