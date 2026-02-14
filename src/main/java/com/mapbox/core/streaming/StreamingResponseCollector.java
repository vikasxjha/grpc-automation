package com.mapbox.core.streaming;

import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Collector for server streaming responses
 * Thread-safe collection of streaming messages
 */
@Slf4j
public class StreamingResponseCollector<T extends Message> implements StreamObserver<T> {

    @Getter
    private final List<T> responses = new ArrayList<>();

    @Getter
    private Throwable error;

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicBoolean completed = new AtomicBoolean(false);

    private final int expectedCount;
    private final CountDownLatch countLatch;

    public StreamingResponseCollector() {
        this.expectedCount = -1;
        this.countLatch = null;
    }

    public StreamingResponseCollector(int expectedCount) {
        this.expectedCount = expectedCount;
        this.countLatch = new CountDownLatch(expectedCount);
    }

    @Override
    public void onNext(T value) {
        synchronized (responses) {
            responses.add(value);
            log.debug("Received streaming message #{}: {}", responses.size(), value);
        }

        if (countLatch != null) {
            countLatch.countDown();
        }
    }

    @Override
    public void onError(Throwable t) {
        log.error("Stream error occurred", t);
        this.error = t;
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        log.info("Stream completed. Received {} messages", responses.size());
        completed.set(true);
        latch.countDown();
    }

    /**
     * Wait for stream completion
     */
    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }

    /**
     * Wait for expected number of messages
     */
    public boolean awaitExpectedCount(long timeout, TimeUnit unit) throws InterruptedException {
        if (countLatch != null) {
            return countLatch.await(timeout, unit);
        }
        return false;
    }

    /**
     * Check if stream completed successfully
     */
    public boolean isCompletedSuccessfully() {
        return completed.get() && error == null;
    }

    /**
     * Get response count
     */
    public int getResponseCount() {
        synchronized (responses) {
            return responses.size();
        }
    }

    /**
     * Get specific response by index
     */
    public T getResponse(int index) {
        synchronized (responses) {
            if (index >= 0 && index < responses.size()) {
                return responses.get(index);
            }
            return null;
        }
    }
}

