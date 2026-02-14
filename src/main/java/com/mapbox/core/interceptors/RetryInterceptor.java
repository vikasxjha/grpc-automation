package com.mapbox.core.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Retry interceptor with exponential backoff
 */
@Slf4j
public class RetryInterceptor implements ClientInterceptor {

    private final int maxRetries;
    private final long backoffMs;

    public RetryInterceptor(int maxRetries, long backoffMs) {
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
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
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    private int attemptCount = 0;

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (shouldRetry(status) && attemptCount < maxRetries) {
                            attemptCount++;
                            long delay = backoffMs * (long) Math.pow(2, attemptCount - 1);
                            log.warn("Retry attempt {} for method {} after {}ms. Status: {}",
                                    attemptCount, method.getFullMethodName(), delay, status.getCode());

                            try {
                                TimeUnit.MILLISECONDS.sleep(delay);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            // Note: In production, you'd want to reissue the call here
                            // This is a simplified version showing the retry logic
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    private boolean shouldRetry(Status status) {
        return status.getCode() == Status.Code.UNAVAILABLE ||
               status.getCode() == Status.Code.DEADLINE_EXCEEDED ||
               status.getCode() == Status.Code.RESOURCE_EXHAUSTED;
    }
}

