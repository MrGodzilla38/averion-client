package dev.averion.client;

import java.util.LinkedHashMap;
import java.util.Map;

final class FeatureRegistry implements AutoCloseable {
    private final Map<String, Feature> features = new LinkedHashMap<>();
    private boolean closed;

    <T extends Feature> T register(T feature) {
        if (closed) {
            throw new IllegalStateException("Feature registry is closed");
        }
        if (features.putIfAbsent(feature.id, feature) != null) {
            throw new IllegalArgumentException("Duplicate feature ID: " + feature.id);
        }
        return feature;
    }

    Feature get(String id) {
        Feature feature = features.get(id);
        if (feature == null) {
            throw new IllegalArgumentException("Unknown feature ID: " + id);
        }
        return feature;
    }

    @Override
    public void close() {
        closed = true;
        try {
            for (Feature feature : features.values()) {
                try {
                    feature.setEnabled(false);
                } catch (RuntimeException failure) {
                    AverionClient.LOGGER.error("Could not stop Averion feature " + feature.id, failure);
                }
            }
        } finally {
            features.clear();
        }
    }
}
