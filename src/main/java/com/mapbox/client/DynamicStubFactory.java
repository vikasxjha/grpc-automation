package com.mapbox.client;

import com.google.protobuf.Message;
import com.mapbox.core.channel.GrpcChannelFactory;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Dynamic stub factory using reflection
 * Enables creating stubs for any gRPC service without compile-time dependencies
 */
@Slf4j
public class DynamicStubFactory {

    private final GrpcChannelFactory channelFactory;

    public DynamicStubFactory(GrpcChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    /**
     * Create blocking stub dynamically using reflection
     *
     * @param serviceGrpcClass The generated service Grpc class (e.g., CampaignServiceGrpc.class)
     * @return Blocking stub instance
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStub<T>> T createBlockingStub(Class<?> serviceGrpcClass) {
        try {
            ManagedChannel channel = channelFactory.getChannel();
            Method newBlockingStubMethod = serviceGrpcClass.getMethod("newBlockingStub", io.grpc.Channel.class);
            return (T) newBlockingStubMethod.invoke(null, channel);
        } catch (Exception e) {
            log.error("Failed to create blocking stub for {}", serviceGrpcClass.getName(), e);
            throw new RuntimeException("Failed to create blocking stub", e);
        }
    }

    /**
     * Create async stub dynamically using reflection
     *
     * @param serviceGrpcClass The generated service Grpc class
     * @return Async stub instance
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStub<T>> T createAsyncStub(Class<?> serviceGrpcClass) {
        try {
            ManagedChannel channel = channelFactory.getChannel();
            Method newStubMethod = serviceGrpcClass.getMethod("newStub", io.grpc.Channel.class);
            return (T) newStubMethod.invoke(null, channel);
        } catch (Exception e) {
            log.error("Failed to create async stub for {}", serviceGrpcClass.getName(), e);
            throw new RuntimeException("Failed to create async stub", e);
        }
    }

    /**
     * Create future stub dynamically using reflection
     *
     * @param serviceGrpcClass The generated service Grpc class
     * @return Future stub instance
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStub<T>> T createFutureStub(Class<?> serviceGrpcClass) {
        try {
            ManagedChannel channel = channelFactory.getChannel();
            Method newFutureStubMethod = serviceGrpcClass.getMethod("newFutureStub", io.grpc.Channel.class);
            return (T) newFutureStubMethod.invoke(null, channel);
        } catch (Exception e) {
            log.error("Failed to create future stub for {}", serviceGrpcClass.getName(), e);
            throw new RuntimeException("Failed to create future stub", e);
        }
    }
}

