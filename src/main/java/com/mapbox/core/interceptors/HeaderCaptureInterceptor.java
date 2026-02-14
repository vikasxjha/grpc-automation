package com.mapbox.core.interceptors;

import io.grpc.*;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor to capture and store response headers and trailers
 */
public class HeaderCaptureInterceptor implements ClientInterceptor {

    @Getter
    private final ConcurrentHashMap<String, Metadata> capturedHeaders = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<String, Metadata> capturedTrailers = new ConcurrentHashMap<>();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String methodName = method.getFullMethodName();

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onHeaders(Metadata headers) {
                        capturedHeaders.put(methodName, headers);
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        capturedTrailers.put(methodName, trailers);
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    public Metadata getHeaders(String methodName) {
        return capturedHeaders.get(methodName);
    }

    public Metadata getTrailers(String methodName) {
        return capturedTrailers.get(methodName);
    }

    public void clear() {
        capturedHeaders.clear();
        capturedTrailers.clear();
    }
}

