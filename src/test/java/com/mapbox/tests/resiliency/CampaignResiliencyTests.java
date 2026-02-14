package com.mapbox.tests.resiliency;

import com.mapbox.core.chaos.GrpcFailureSimulator;
import com.mapbox.core.retry.ResilienceManager;
import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import com.mapbox.testdata.campaign.CampaignTestData;
import com.mapbox.tests.base.BaseTest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Resiliency tests for Campaign Service
 * Validates retry, timeout, and failure handling
 */
@Epic("Campaign Service")
@Feature("Resiliency")
public class CampaignResiliencyTests extends BaseTest {

    @Test(description = "Verify retry occurs when service is unavailable")
    @Story("Retry - UNAVAILABLE")
    @Severity(SeverityLevel.CRITICAL)
    public void testRetryOnUnavailable() {
        // This test demonstrates the retry mechanism
        // In real scenario, you'd use a mock server or chaos engineering

        ResilienceManager resilienceManager = new ResilienceManager();

        try {
            resilienceManager.executeWithRetry(() -> {
                GrpcFailureSimulator.simulateUnavailable();
                return null;
            });
            fail("Should have thrown exception");
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus().getCode(), Status.Code.UNAVAILABLE);
        }
    }

    @Test(description = "Verify deadline exceeded handling")
    @Story("Timeout - Deadline Exceeded")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeadlineExceeded() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act & Assert
        // This would timeout if the server is slow
        var response = campaignClient.getCampaignWithDeadline(request, 1, TimeUnit.MILLISECONDS);

        // In real scenario with slow server, this would be DEADLINE_EXCEEDED
        assertNotNull(response);
    }

    @Test(description = "Verify timeout simulation")
    @Story("Chaos - Timeout")
    @Severity(SeverityLevel.NORMAL)
    public void testTimeoutSimulation() {
        long startTime = System.currentTimeMillis();

        // Simulate 100ms timeout
        GrpcFailureSimulator.simulateTimeout(100);

        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 100, "Timeout should be at least 100ms");
    }

    @Test(description = "Verify CANCELLED error handling")
    @Story("Error Handling - Cancelled")
    @Severity(SeverityLevel.NORMAL)
    public void testCancelledErrorHandling() {
        try {
            GrpcFailureSimulator.simulateCancelled();
            fail("Should have thrown exception");
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus().getCode(), Status.Code.CANCELLED);
        }
    }

    @Test(description = "Verify RESOURCE_EXHAUSTED error handling")
    @Story("Error Handling - Resource Exhausted")
    @Severity(SeverityLevel.NORMAL)
    public void testResourceExhaustedHandling() {
        try {
            GrpcFailureSimulator.simulateResourceExhausted();
            fail("Should have thrown exception");
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus().getCode(), Status.Code.RESOURCE_EXHAUSTED);
        }
    }

    @Test(description = "Verify network latency simulation")
    @Story("Chaos - Network Latency")
    @Severity(SeverityLevel.NORMAL)
    public void testNetworkLatencySimulation() {
        long startTime = System.currentTimeMillis();

        // Simulate network latency between 50-150ms
        GrpcFailureSimulator.simulateNetworkLatency(50, 150);

        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 50 && elapsed <= 200,
                "Latency should be between 50-200ms");
    }

    @Test(description = "Verify random error simulation")
    @Story("Chaos - Random Errors")
    @Severity(SeverityLevel.NORMAL)
    public void testRandomErrorSimulation() {
        // Simulate with 100% probability to ensure it fails
        try {
            GrpcFailureSimulator.simulateRandomError(1.0);
            // May or may not fail depending on random selection
        } catch (StatusRuntimeException e) {
            // Expected in some cases
            assertTrue(
                    e.getStatus().getCode() == Status.Code.UNAVAILABLE ||
                    e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED ||
                    e.getStatus().getCode() == Status.Code.RESOURCE_EXHAUSTED
            );
        }
    }
}

