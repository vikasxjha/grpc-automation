package com.mapbox.core.chaos;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Chaos engineering utilities for simulating gRPC failures
 */
@Slf4j
public class GrpcFailureSimulator {

    private static final Random random = new Random();

    private GrpcFailureSimulator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Simulate UNAVAILABLE error
     */
    public static void simulateUnavailable() {
        log.warn("Simulating UNAVAILABLE error");
        throw new StatusRuntimeException(Status.UNAVAILABLE
                .withDescription("Service temporarily unavailable"));
    }

    /**
     * Simulate DEADLINE_EXCEEDED error
     */
    public static void simulateDeadlineExceeded() {
        log.warn("Simulating DEADLINE_EXCEEDED error");
        throw new StatusRuntimeException(Status.DEADLINE_EXCEEDED
                .withDescription("Request deadline exceeded"));
    }

    /**
     * Simulate timeout by sleeping
     */
    public static void simulateTimeout(long timeoutMs) {
        log.warn("Simulating timeout of {}ms", timeoutMs);
        try {
            TimeUnit.MILLISECONDS.sleep(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during timeout simulation", e);
        }
    }

    /**
     * Simulate CANCELLED error
     */
    public static void simulateCancelled() {
        log.warn("Simulating CANCELLED error");
        throw new StatusRuntimeException(Status.CANCELLED
                .withDescription("Request cancelled by client"));
    }

    /**
     * Simulate RESOURCE_EXHAUSTED error
     */
    public static void simulateResourceExhausted() {
        log.warn("Simulating RESOURCE_EXHAUSTED error");
        throw new StatusRuntimeException(Status.RESOURCE_EXHAUSTED
                .withDescription("Server resource exhausted"));
    }

    /**
     * Simulate INVALID_ARGUMENT error
     */
    public static void simulateInvalidArgument(String message) {
        log.warn("Simulating INVALID_ARGUMENT error: {}", message);
        throw new StatusRuntimeException(Status.INVALID_ARGUMENT
                .withDescription(message));
    }

    /**
     * Simulate NOT_FOUND error
     */
    public static void simulateNotFound(String resourceId) {
        log.warn("Simulating NOT_FOUND error for resource: {}", resourceId);
        throw new StatusRuntimeException(Status.NOT_FOUND
                .withDescription("Resource not found: " + resourceId));
    }

    /**
     * Simulate PERMISSION_DENIED error
     */
    public static void simulatePermissionDenied() {
        log.warn("Simulating PERMISSION_DENIED error");
        throw new StatusRuntimeException(Status.PERMISSION_DENIED
                .withDescription("Permission denied"));
    }

    /**
     * Simulate UNAUTHENTICATED error
     */
    public static void simulateUnauthenticated() {
        log.warn("Simulating UNAUTHENTICATED error");
        throw new StatusRuntimeException(Status.UNAUTHENTICATED
                .withDescription("Authentication required"));
    }

    /**
     * Simulate random error with probability
     */
    public static void simulateRandomError(double probability) {
        if (random.nextDouble() < probability) {
            Status.Code[] errorCodes = {
                Status.Code.UNAVAILABLE,
                Status.Code.DEADLINE_EXCEEDED,
                Status.Code.RESOURCE_EXHAUSTED
            };

            Status.Code randomCode = errorCodes[random.nextInt(errorCodes.length)];
            log.warn("Simulating random error: {}", randomCode);
            throw new StatusRuntimeException(Status.fromCode(randomCode)
                    .withDescription("Simulated random error"));
        }
    }

    /**
     * Simulate intermittent failure based on count
     */
    public static void simulateIntermittentFailure(int callCount, int failEveryN) {
        if (callCount % failEveryN == 0) {
            simulateUnavailable();
        }
    }

    /**
     * Simulate network latency
     */
    public static void simulateNetworkLatency(long minMs, long maxMs) {
        long latency = minMs + random.nextInt((int) (maxMs - minMs + 1));
        log.debug("Simulating network latency of {}ms", latency);
        try {
            TimeUnit.MILLISECONDS.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during latency simulation", e);
        }
    }
}

