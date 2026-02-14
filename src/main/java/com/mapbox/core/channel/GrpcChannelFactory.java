package com.mapbox.core.channel;

import com.mapbox.core.config.ConfigManager;
import com.mapbox.core.config.FrameworkConfig;
import com.mapbox.core.interceptors.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Enterprise-grade ManagedChannel factory with connection pooling
 * Provides thread-safe channel creation with custom interceptors and configurations
 */
@Slf4j
public class GrpcChannelFactory {

    private static final ConcurrentHashMap<String, ManagedChannel> channelPool = new ConcurrentHashMap<>();
    private final FrameworkConfig config;
    private final List<ClientInterceptor> interceptors;

    public GrpcChannelFactory() {
        this.config = ConfigManager.getConfig();
        this.interceptors = new ArrayList<>();
        initializeDefaultInterceptors();
    }

    /**
     * Initialize default interceptors for all channels
     */
    private void initializeDefaultInterceptors() {
        if (config.logRequestEnabled() || config.logResponseEnabled()) {
            interceptors.add(new LoggingInterceptor());
        }

        if (config.authEnabled()) {
            interceptors.add(new AuthTokenInterceptor(config.authToken()));
        }

        if (config.metricsEnabled()) {
            interceptors.add(new MetricsInterceptor());
        }

        interceptors.add(new HeaderCaptureInterceptor());
        interceptors.add(new RetryInterceptor(config.maxRetryAttempts(), config.retryBackoffMs()));
    }

    /**
     * Get or create a managed channel for the given target
     * Implements connection pooling for efficiency
     */
    public ManagedChannel getChannel(String host, int port) {
        String channelKey = host + ":" + port;
        return channelPool.computeIfAbsent(channelKey, key -> createChannel(host, port));
    }

    /**
     * Get channel using default configuration
     */
    public ManagedChannel getChannel() {
        return getChannel(config.grpcHost(), config.grpcPort());
    }

    /**
     * Create a new managed channel with all configurations
     */
    private ManagedChannel createChannel(String host, int port) {
        log.info("Creating new gRPC channel for {}:{}", host, port);

        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(host, port)
                .maxInboundMessageSize(config.maxInboundMessageSize())
                .keepAliveTime(config.keepAliveTimeSeconds(), TimeUnit.SECONDS)
                .keepAliveTimeout(config.keepAliveTimeoutSeconds(), TimeUnit.SECONDS)
                .idleTimeout(config.idleTimeoutSeconds(), TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true);

        // TLS configuration
        if (config.tlsEnabled()) {
            // In production, configure TLS with proper certificates
            channelBuilder.useTransportSecurity();
        } else {
            channelBuilder.usePlaintext();
        }

        // Add all interceptors
        channelBuilder.intercept(interceptors);

        return channelBuilder.build();
    }

    /**
     * Add custom interceptor to the factory
     */
    public GrpcChannelFactory withInterceptor(ClientInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    /**
     * Shutdown all channels in the pool
     */
    public void shutdownAll() {
        log.info("Shutting down all gRPC channels");
        channelPool.values().forEach(channel -> {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error shutting down channel", e);
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        });
        channelPool.clear();
    }

    /**
     * Shutdown specific channel
     */
    public void shutdownChannel(String host, int port) {
        String channelKey = host + ":" + port;
        ManagedChannel channel = channelPool.remove(channelKey);
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error shutting down channel {}", channelKey, e);
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        }
    }

    /**
     * Get channel state
     */
    public ConnectivityState getChannelState(String host, int port) {
        String channelKey = host + ":" + port;
        ManagedChannel channel = channelPool.get(channelKey);
        return channel != null ? channel.getState(false) : ConnectivityState.SHUTDOWN;
    }
}

