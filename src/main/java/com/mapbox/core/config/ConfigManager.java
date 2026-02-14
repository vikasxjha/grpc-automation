package com.mapbox.core.config;

import org.aeonbits.owner.ConfigCache;

/**
 * Centralized configuration manager for the framework
 * Provides thread-safe singleton access to framework configuration
 */
public final class ConfigManager {

    private static volatile FrameworkConfig config;

    private ConfigManager() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get framework configuration instance
     * Thread-safe lazy initialization with double-checked locking
     */
    public static FrameworkConfig getConfig() {
        if (config == null) {
            synchronized (ConfigManager.class) {
                if (config == null) {
                    config = ConfigCache.getOrCreate(FrameworkConfig.class);
                }
            }
        }
        return config;
    }

    /**
     * Reset configuration (useful for testing)
     */
    public static void resetConfig() {
        synchronized (ConfigManager.class) {
            config = null;
        }
    }
}

