package com.mapbox.utils;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Snapshot testing utility for proto messages
 * Enables regression testing by comparing against baseline snapshots
 */
@Slf4j
public class SnapshotManager {

    private static final String SNAPSHOT_DIR = "src/test/resources/snapshots";
    private final JsonFormat.Printer jsonPrinter;
    private final JsonFormat.Parser jsonParser;

    public SnapshotManager() {
        this.jsonPrinter = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .sortingMapKeys();

        this.jsonParser = JsonFormat.parser()
                .ignoringUnknownFields();

        createSnapshotDirectory();
    }

    /**
     * Save message as snapshot
     */
    public void saveSnapshot(String snapshotName, Message message) {
        try {
            String json = jsonPrinter.print(message);
            Path snapshotPath = getSnapshotPath(snapshotName);
            Files.writeString(snapshotPath, json);
            log.info("Saved snapshot: {}", snapshotName);
        } catch (Exception e) {
            log.error("Failed to save snapshot: {}", snapshotName, e);
            throw new RuntimeException("Failed to save snapshot", e);
        }
    }

    /**
     * Load snapshot from file
     */
    public <T extends Message> T loadSnapshot(String snapshotName, Message.Builder builder) {
        try {
            Path snapshotPath = getSnapshotPath(snapshotName);
            if (!Files.exists(snapshotPath)) {
                throw new RuntimeException("Snapshot not found: " + snapshotName);
            }

            String json = Files.readString(snapshotPath);
            jsonParser.merge(json, builder);

            @SuppressWarnings("unchecked")
            T message = (T) builder.build();
            return message;
        } catch (Exception e) {
            log.error("Failed to load snapshot: {}", snapshotName, e);
            throw new RuntimeException("Failed to load snapshot", e);
        }
    }

    /**
     * Compare message with snapshot
     */
    public boolean matchesSnapshot(String snapshotName, Message message) {
        try {
            String currentJson = jsonPrinter.print(message);
            Path snapshotPath = getSnapshotPath(snapshotName);

            if (!Files.exists(snapshotPath)) {
                log.warn("Snapshot not found, creating new: {}", snapshotName);
                saveSnapshot(snapshotName, message);
                return true;
            }

            String snapshotJson = Files.readString(snapshotPath);
            return currentJson.equals(snapshotJson);
        } catch (Exception e) {
            log.error("Failed to compare snapshot: {}", snapshotName, e);
            return false;
        }
    }

    /**
     * Save descriptor as baseline schema
     */
    public void saveSchemaBaseline(String schemaName, Descriptors.Descriptor descriptor) {
        try {
            String schema = descriptor.toProto().toString();
            Path schemaPath = getSchemaPath(schemaName);
            Files.writeString(schemaPath, schema);
            log.info("Saved schema baseline: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to save schema baseline: {}", schemaName, e);
            throw new RuntimeException("Failed to save schema baseline", e);
        }
    }

    /**
     * Get snapshot file path
     */
    private Path getSnapshotPath(String snapshotName) {
        return Paths.get(SNAPSHOT_DIR, snapshotName + ".json");
    }

    /**
     * Get schema file path
     */
    private Path getSchemaPath(String schemaName) {
        return Paths.get(SNAPSHOT_DIR, "schemas", schemaName + ".txt");
    }

    /**
     * Create snapshot directory if it doesn't exist
     */
    private void createSnapshotDirectory() {
        try {
            Files.createDirectories(Paths.get(SNAPSHOT_DIR, "schemas"));
        } catch (IOException e) {
            log.error("Failed to create snapshot directory", e);
        }
    }
}

