package com.mapbox.core.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication interceptor that adds auth token to metadata
 */
@Slf4j
public class AuthTokenInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> AUTH_TOKEN_KEY =
        Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final String authToken;

    public AuthTokenInterceptor(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (authToken != null && !authToken.isEmpty()) {
                    headers.put(AUTH_TOKEN_KEY, "Bearer " + authToken);
                    log.debug("Added auth token to request");
                }
                super.start(responseListener, headers);
            }
        };
    }
}

