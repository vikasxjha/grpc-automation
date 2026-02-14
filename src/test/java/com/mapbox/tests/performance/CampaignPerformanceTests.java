package com.mapbox.tests.performance;

import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import com.mapbox.testdata.campaign.CampaignTestData;
import com.mapbox.tests.base.BaseTest;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

import static org.testng.Assert.*;

/**
 * Performance tests for Campaign Service
 * Validates latency, throughput, and performance under load
 */
@Slf4j
@Epic("Campaign Service")
@Feature("Performance")
public class CampaignPerformanceTests extends BaseTest {

    @Test(description = "Verify P99 latency is under 500ms")
    @Story("Performance - P99 Latency")
    @Severity(SeverityLevel.CRITICAL)
    public void testP99LatencyUnder500ms() {
        // Arrange
        int iterations = 100;
        List<Long> latencies = new ArrayList<>();

        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act - Execute multiple requests
        for (int i = 0; i < iterations; i++) {
            GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);
            if (response.isSuccess()) {
                latencies.add(response.getLatencyMs());
            }
        }

        // Calculate P99
        latencies.sort(Long::compareTo);
        int p99Index = (int) Math.ceil(iterations * 0.99) - 1;
        long p99Latency = latencies.get(p99Index);

        log.info("P99 Latency: {}ms", p99Latency);

        // Assert
        assertTrue(p99Latency < 500,
                "P99 latency should be under 500ms, was: " + p99Latency + "ms");
    }

    @Test(description = "Verify average latency is acceptable")
    @Story("Performance - Average Latency")
    @Severity(SeverityLevel.NORMAL)
    public void testAverageLatency() {
        // Arrange
        int iterations = 50;
        List<Long> latencies = new ArrayList<>();

        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        for (int i = 0; i < iterations; i++) {
            GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);
            if (response.isSuccess()) {
                latencies.add(response.getLatencyMs());
            }
        }

        // Calculate statistics
        LongSummaryStatistics stats = latencies.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();

        log.info("Latency Stats - Min: {}ms, Max: {}ms, Avg: {}ms",
                stats.getMin(), stats.getMax(), stats.getAverage());

        // Assert
        assertTrue(stats.getAverage() < 200,
                "Average latency should be under 200ms");
    }

    @Test(description = "Verify consistent performance across multiple calls")
    @Story("Performance - Consistency")
    @Severity(SeverityLevel.NORMAL)
    public void testPerformanceConsistency() {
        // Arrange
        int iterations = 30;
        List<Long> latencies = new ArrayList<>();

        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        for (int i = 0; i < iterations; i++) {
            GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);
            if (response.isSuccess()) {
                latencies.add(response.getLatencyMs());
            }
        }

        // Calculate coefficient of variation (CV = stddev / mean)
        LongSummaryStatistics stats = latencies.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();

        double mean = stats.getAverage();
        double variance = latencies.stream()
                .mapToDouble(l -> Math.pow(l - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / mean;

        log.info("Performance Consistency - CV: {}", cv);

        // Assert - CV should be less than 0.5 for consistent performance
        assertTrue(cv < 0.5,
                "Coefficient of variation should be less than 0.5 for consistent performance");
    }

    @Test(description = "Verify throughput under concurrent load")
    @Story("Performance - Throughput")
    @Severity(SeverityLevel.NORMAL)
    public void testThroughputUnderLoad() throws InterruptedException {
        // Arrange
        int totalRequests = 100;
        List<Thread> threads = new ArrayList<>();
        List<Long> latencies = new ArrayList<>();

        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        long startTime = System.currentTimeMillis();

        // Act - Execute concurrent requests
        for (int i = 0; i < totalRequests; i++) {
            Thread thread = new Thread(() -> {
                GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);
                synchronized (latencies) {
                    latencies.add(response.getLatencyMs());
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double throughput = (double) totalRequests / (totalTime / 1000.0);

        log.info("Throughput: {} requests/second", throughput);

        // Assert - Should handle at least 10 requests per second
        assertTrue(throughput >= 10,
                "Throughput should be at least 10 req/s, was: " + throughput);
    }
}

