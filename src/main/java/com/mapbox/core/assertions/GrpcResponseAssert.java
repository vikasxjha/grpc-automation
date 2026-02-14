package com.mapbox.core.assertions;

import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Custom assertion DSL for gRPC responses
 * Provides fluent API for validating responses, status codes, metadata, and latency
 */
@Slf4j
public class GrpcResponseAssert<T extends Message> extends AbstractAssert<GrpcResponseAssert<T>, GrpcResponse<T>> {

    public GrpcResponseAssert(GrpcResponse<T> actual) {
        super(actual, GrpcResponseAssert.class);
    }

    public static <T extends Message> GrpcResponseAssert<T> assertThat(GrpcResponse<T> actual) {
        return new GrpcResponseAssert<>(actual);
    }

    /**
     * Assert response status is OK
     */
    public GrpcResponseAssert<T> hasStatusOk() {
        isNotNull();
        if (actual.getStatus().getCode() != Status.Code.OK) {
            failWithMessage("Expected status OK but was <%s> with description: %s",
                    actual.getStatus().getCode(), actual.getStatus().getDescription());
        }
        return this;
    }

    /**
     * Assert specific status code
     */
    public GrpcResponseAssert<T> hasStatus(Status.Code expectedCode) {
        isNotNull();
        if (actual.getStatus().getCode() != expectedCode) {
            failWithMessage("Expected status <%s> but was <%s>",
                    expectedCode, actual.getStatus().getCode());
        }
        return this;
    }

    /**
     * Assert field value
     */
    public GrpcResponseAssert<T> hasField(String fieldName, Object expectedValue) {
        isNotNull();
        hasStatusOk();

        Object actualValue = getFieldValue(actual.getResponse(), fieldName);
        if (!expectedValue.equals(actualValue)) {
            failWithMessage("Expected field <%s> to be <%s> but was <%s>",
                    fieldName, expectedValue, actualValue);
        }
        return this;
    }

    /**
     * Assert field exists
     */
    public GrpcResponseAssert<T> hasFieldPresent(String fieldName) {
        isNotNull();
        hasStatusOk();

        Object actualValue = getFieldValue(actual.getResponse(), fieldName);
        if (actualValue == null) {
            failWithMessage("Expected field <%s> to be present but was null", fieldName);
        }
        return this;
    }

    /**
     * Assert field is null or empty
     */
    public GrpcResponseAssert<T> hasFieldEmpty(String fieldName) {
        isNotNull();
        hasStatusOk();

        Object actualValue = getFieldValue(actual.getResponse(), fieldName);
        if (actualValue != null && !actualValue.toString().isEmpty()) {
            failWithMessage("Expected field <%s> to be empty but was <%s>",
                    fieldName, actualValue);
        }
        return this;
    }

    /**
     * Assert latency is less than threshold
     */
    public GrpcResponseAssert<T> latencyLessThan(long maxLatencyMs) {
        isNotNull();
        if (actual.getLatencyMs() >= maxLatencyMs) {
            failWithMessage("Expected latency < %dms but was %dms",
                    maxLatencyMs, actual.getLatencyMs());
        }
        return this;
    }

    /**
     * Assert latency is greater than threshold
     */
    public GrpcResponseAssert<T> latencyGreaterThan(long minLatencyMs) {
        isNotNull();
        if (actual.getLatencyMs() <= minLatencyMs) {
            failWithMessage("Expected latency > %dms but was %dms",
                    minLatencyMs, actual.getLatencyMs());
        }
        return this;
    }

    /**
     * Assert map contains key
     */
    public GrpcResponseAssert<T> containsKey(String fieldName, String key) {
        isNotNull();
        hasStatusOk();

        Object fieldValue = getFieldValue(actual.getResponse(), fieldName);
        if (!(fieldValue instanceof Map)) {
            failWithMessage("Field <%s> is not a Map", fieldName);
        }

        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>) fieldValue;
        if (!map.containsKey(key)) {
            failWithMessage("Expected map field <%s> to contain key <%s>", fieldName, key);
        }
        return this;
    }

    /**
     * Assert repeated field size
     */
    public GrpcResponseAssert<T> hasRepeatedFieldSize(String fieldName, int expectedSize) {
        isNotNull();
        hasStatusOk();

        Object fieldValue = getFieldValue(actual.getResponse(), fieldName);
        if (!(fieldValue instanceof List)) {
            failWithMessage("Field <%s> is not a repeated field", fieldName);
        }

        @SuppressWarnings("unchecked")
        List<?> list = (List<?>) fieldValue;
        if (list.size() != expectedSize) {
            failWithMessage("Expected repeated field <%s> size to be <%d> but was <%d>",
                    fieldName, expectedSize, list.size());
        }
        return this;
    }

    /**
     * Assert metadata header exists
     */
    public GrpcResponseAssert<T> hasHeader(String headerKey) {
        isNotNull();
        Metadata headers = actual.getHeaders();
        if (headers == null || !headers.containsKey(Metadata.Key.of(headerKey, Metadata.ASCII_STRING_MARSHALLER))) {
            failWithMessage("Expected header <%s> to be present", headerKey);
        }
        return this;
    }

    /**
     * Assert metadata header value
     */
    public GrpcResponseAssert<T> hasHeaderValue(String headerKey, String expectedValue) {
        isNotNull();
        Metadata headers = actual.getHeaders();
        if (headers == null) {
            failWithMessage("Headers are null");
        }

        Metadata.Key<String> key = Metadata.Key.of(headerKey, Metadata.ASCII_STRING_MARSHALLER);
        String actualValue = headers.get(key);
        if (!expectedValue.equals(actualValue)) {
            failWithMessage("Expected header <%s> to be <%s> but was <%s>",
                    headerKey, expectedValue, actualValue);
        }
        return this;
    }

    /**
     * Assert error description contains text
     */
    public GrpcResponseAssert<T> hasErrorDescriptionContaining(String text) {
        isNotNull();
        String description = actual.getStatus().getDescription();
        if (description == null || !description.contains(text)) {
            failWithMessage("Expected error description to contain <%s> but was <%s>",
                    text, description);
        }
        return this;
    }

    /**
     * Get field value using reflection
     */
    private Object getFieldValue(T message, String fieldName) {
        try {
            // Convert snake_case to camelCase for getter method
            String methodName = "get" + toCamelCase(fieldName);
            Method method = message.getClass().getMethod(methodName);
            return method.invoke(message);
        } catch (Exception e) {
            log.error("Failed to get field value for {}", fieldName, e);
            return null;
        }
    }

    /**
     * Convert snake_case to CamelCase
     */
    private String toCamelCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}

