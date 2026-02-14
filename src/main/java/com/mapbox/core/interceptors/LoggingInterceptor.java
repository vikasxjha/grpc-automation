package com.mapbox.core.interceptors;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Logging interceptor that logs gRPC requests and responses in JSON format
 */
@Slf4j
public class LoggingInterceptor implements ClientInterceptor {

    private final JsonFormat.Printer jsonPrinter;

    public LoggingInterceptor() {
        this.jsonPrinter = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames();
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {
                logRequest(method.getFullMethodName(), message);
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onMessage(RespT message) {
                        logResponse(method.getFullMethodName(), message);
                        super.onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (!status.isOk()) {
                            log.error("gRPC call failed - Method: {}, Status: {}, Description: {}",
                                    method.getFullMethodName(), status.getCode(), status.getDescription());
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    private <T> void logRequest(String methodName, T message) {
        try {
            log.info("\n=== gRPC REQUEST ===\nMethod: {}\nPayload: {}",
                    methodName, formatMessage(message));
        } catch (Exception e) {
            log.warn("Failed to log request", e);
        }
    }

    private <T> void logResponse(String methodName, T message) {
        try {
            log.info("\n=== gRPC RESPONSE ===\nMethod: {}\nPayload: {}",
                    methodName, formatMessage(message));
        } catch (Exception e) {
            log.warn("Failed to log response", e);
        }
    }

    private <T> String formatMessage(T message) {
        try {
            if (message instanceof Message) {
                return jsonPrinter.print((Message) message);
            }
            return message.toString();
        } catch (Exception e) {
            return message.toString();
        }
    }
}

