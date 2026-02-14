package com.mapbox.core.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Resilience manager for retry and circuit breaker patterns
 */
@Slf4j
public class ResilienceManager {

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public ResilienceManager() {
        this.retry = createRetry();
        this.circuitBreaker = createCircuitBreaker();
    }

    /**
     * Create retry configuration
     */
    private Retry createRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(StatusRuntimeException.class)
                .retryOnException(throwable -> {
                    if (throwable instanceof StatusRuntimeException) {
                        Status.Code code = ((StatusRuntimeException) throwable).getStatus().getCode();
                        return code == Status.Code.UNAVAILABLE ||
                               code == Status.Code.DEADLINE_EXCEEDED ||
                               code == Status.Code.RESOURCE_EXHAUSTED;
                    }
                    return false;
                })
                .build();

        Retry retry = Retry.of("grpcRetry", config);

        retry.getEventPublisher()
                .onRetry(event -> log.warn("Retry attempt {} due to {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getMessage()));

        return retry;
    }

    /**
     * Create circuit breaker configuration
     */
    private CircuitBreaker createCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("grpcCircuitBreaker", config);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Circuit breaker state changed from {} to {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    /**
     * Execute supplier with retry
     */
    public <T> T executeWithRetry(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = Retry.decorateSupplier(retry, supplier);
        return decoratedSupplier.get();
    }

    /**
     * Execute supplier with circuit breaker
     */
    public <T> T executeWithCircuitBreaker(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        return decoratedSupplier.get();
    }

    /**
     * Execute with both retry and circuit breaker
     */
    public <T> T executeWithResilience(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker,
                        Retry.decorateSupplier(retry, supplier));
        return decoratedSupplier.get();
    }

    /**
     * Get retry instance for custom decoration
     */
    public Retry getRetry() {
        return retry;
    }

    /**
     * Get circuit breaker instance for custom decoration
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}

