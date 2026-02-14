package com.mapbox.core.config;

import org.aeonbits.owner.Config;

/**
 * Framework configuration interface using Owner library
 * Supports multiple environments and property override
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
    "system:properties",
    "system:env",
    "classpath:config/${env}.properties",
    "classpath:config/default.properties"
})
public interface FrameworkConfig extends Config {

    @Key("grpc.host")
    @DefaultValue("localhost")
    String grpcHost();

    @Key("grpc.port")
    @DefaultValue("9090")
    int grpcPort();

    @Key("grpc.tls.enabled")
    @DefaultValue("false")
    boolean tlsEnabled();

    @Key("grpc.tls.cert.path")
    String tlsCertPath();

    @Key("grpc.keepalive.time.seconds")
    @DefaultValue("30")
    long keepAliveTimeSeconds();

    @Key("grpc.keepalive.timeout.seconds")
    @DefaultValue("10")
    long keepAliveTimeoutSeconds();

    @Key("grpc.idle.timeout.seconds")
    @DefaultValue("300")
    long idleTimeoutSeconds();

    @Key("grpc.max.inbound.message.size")
    @DefaultValue("4194304")
    int maxInboundMessageSize();

    @Key("grpc.max.retry.attempts")
    @DefaultValue("3")
    int maxRetryAttempts();

    @Key("grpc.retry.backoff.ms")
    @DefaultValue("1000")
    long retryBackoffMs();

    @Key("grpc.deadline.seconds")
    @DefaultValue("30")
    long deadlineSeconds();

    @Key("auth.token")
    String authToken();

    @Key("auth.enabled")
    @DefaultValue("false")
    boolean authEnabled();

    @Key("logging.request.enabled")
    @DefaultValue("true")
    boolean logRequestEnabled();

    @Key("logging.response.enabled")
    @DefaultValue("true")
    boolean logResponseEnabled();

    @Key("metrics.enabled")
    @DefaultValue("true")
    boolean metricsEnabled();

    @Key("parallel.execution.threads")
    @DefaultValue("5")
    int parallelExecutionThreads();

    @Key("environment")
    @DefaultValue("dev")
    String environment();
}

