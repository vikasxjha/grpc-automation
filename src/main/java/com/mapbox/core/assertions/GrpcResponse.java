package com.mapbox.core.assertions;

import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Container for gRPC response with metadata
 */
@Data
@Builder
@AllArgsConstructor
public class GrpcResponse<T extends Message> {
    private T response;
    private Status status;
    private Metadata headers;
    private Metadata trailers;
    private long latencyMs;

    public static <T extends Message> GrpcResponse<T> success(T response, long latencyMs) {
        return GrpcResponse.<T>builder()
                .response(response)
                .status(Status.OK)
                .latencyMs(latencyMs)
                .build();
    }

    public static <T extends Message> GrpcResponse<T> failure(Status status, long latencyMs) {
        return GrpcResponse.<T>builder()
                .status(status)
                .latencyMs(latencyMs)
                .build();
    }

    public boolean isSuccess() {
        return status != null && status.isOk();
    }
}

