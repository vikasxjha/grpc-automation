package com.mapbox.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test data cache for sharing data across tests
 * Thread-safe storage for test context and state
 */
@Slf4j
public class TestDataCache {

    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    private TestDataCache() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Store value in cache
     */
    public static void put(String key, Object value) {
        cache.put(key, value);
        log.debug("Cached value for key: {}", key);
    }

    /**
     * Retrieve value from cache
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) cache.get(key);
    }

    /**
     * Retrieve value with default
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        return (T) cache.getOrDefault(key, defaultValue);
    }

    /**
     * Check if key exists
     */
    public static boolean contains(String key) {
        return cache.containsKey(key);
    }

    /**
     * Remove value from cache
     */
    public static void remove(String key) {
        cache.remove(key);
        log.debug("Removed cached value for key: {}", key);
    }

    /**
     * Clear entire cache
     */
    public static void clear() {
        cache.clear();
        log.debug("Cleared test data cache");
    }

    /**
     * Get cache size
     */
    public static int size() {
        return cache.size();
    }
}

