package com.mapbox.core.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics interceptor for tracking gRPC call statistics
 */
@Slf4j
public class MetricsInterceptor implements ClientInterceptor {

    private final ConcurrentHashMap<String, CallMetrics> metricsMap = new ConcurrentHashMap<>();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String methodName = method.getFullMethodName();
        CallMetrics metrics = metricsMap.computeIfAbsent(methodName, k -> new CallMetrics());

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            private long startTime;

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                startTime = System.currentTimeMillis();
                metrics.incrementTotalCalls();

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        long latency = System.currentTimeMillis() - startTime;
                        metrics.recordLatency(latency);

                        if (status.isOk()) {
                            metrics.incrementSuccessfulCalls();
                        } else {
                            metrics.incrementFailedCalls();
                        }

                        log.debug("Method: {}, Latency: {}ms, Status: {}",
                                methodName, latency, status.getCode());

                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    public CallMetrics getMetrics(String methodName) {
        return metricsMap.get(methodName);
    }

    public void resetMetrics() {
        metricsMap.clear();
    }

    /**
     * Metrics data class
     */
    public static class CallMetrics {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxLatency = new AtomicLong(0);

        public void incrementTotalCalls() {
            totalCalls.incrementAndGet();
        }

        public void incrementSuccessfulCalls() {
            successfulCalls.incrementAndGet();
        }

        public void incrementFailedCalls() {
            failedCalls.incrementAndGet();
        }

        public void recordLatency(long latency) {
            totalLatency.addAndGet(latency);
            minLatency.updateAndGet(current -> Math.min(current, latency));
            maxLatency.updateAndGet(current -> Math.max(current, latency));
        }

        public long getTotalCalls() {
            return totalCalls.get();
        }

        public long getSuccessfulCalls() {
            return successfulCalls.get();
        }

        public long getFailedCalls() {
            return failedCalls.get();
        }

        public double getAverageLatency() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalLatency.get() / total : 0;
        }

        public long getMinLatency() {
            long min = minLatency.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMaxLatency() {
            return maxLatency.get();
        }

        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total * 100 : 0;
        }
    }
}

