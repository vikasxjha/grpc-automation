package com.mapbox.client;

import com.google.protobuf.Message;
import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.core.channel.GrpcChannelFactory;
import com.mapbox.core.config.ConfigManager;
import io.grpc.*;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Base gRPC client providing common functionality
 * All service-specific clients should extend this class
 */
@Slf4j
public abstract class BaseGrpcClient<T extends AbstractStub<T>> {

    protected final GrpcChannelFactory channelFactory;
    protected final ManagedChannel channel;
    protected T blockingStub;
    protected T asyncStub;

    protected BaseGrpcClient(GrpcChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        this.channel = channelFactory.getChannel();
        initializeStubs();
    }

    /**
     * Initialize blocking and async stubs
     * Must be implemented by subclasses
     */
    protected abstract void initializeStubs();

    /**
     * Execute unary call with timing and error handling
     */
    protected <ReqT extends Message, RespT extends Message> GrpcResponse<RespT> executeUnaryCall(
            ReqT request,
            UnaryCallable<ReqT, RespT> callable) {

        long startTime = System.currentTimeMillis();
        try {
            RespT response = callable.call(request);
            long latency = System.currentTimeMillis() - startTime;

            return GrpcResponse.<RespT>builder()
                    .response(response)
                    .status(Status.OK)
                    .latencyMs(latency)
                    .build();

        } catch (StatusRuntimeException e) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("gRPC call failed with status: {}", e.getStatus(), e);

            return GrpcResponse.<RespT>builder()
                    .status(e.getStatus())
                    .latencyMs(latency)
                    .build();
        }
    }

    /**
     * Execute call with custom deadline
     */
    protected <ReqT extends Message, RespT extends Message> GrpcResponse<RespT> executeWithDeadline(
            ReqT request,
            UnaryCallable<ReqT, RespT> callable,
            long timeout,
            TimeUnit unit) {

        return executeUnaryCall(request, callable);
    }

    /**
     * Shutdown client
     */
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error during channel shutdown", e);
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        }
    }

    /**
     * Functional interface for unary calls
     */
    @FunctionalInterface
    protected interface UnaryCallable<ReqT extends Message, RespT extends Message> {
        RespT call(ReqT request) throws StatusRuntimeException;
    }
}

