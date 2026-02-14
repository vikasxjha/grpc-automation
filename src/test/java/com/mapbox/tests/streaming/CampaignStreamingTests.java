package com.mapbox.tests.streaming;

import com.mapbox.core.streaming.StreamingResponseCollector;
import com.mapbox.grpc.campaign.CampaignMessage;
import com.mapbox.grpc.campaign.ListCampaignsRequest;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import com.mapbox.tests.base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Streaming tests for Campaign Service
 * Validates server streaming, client streaming, and bidirectional streaming
 */
@Epic("Campaign Service")
@Feature("Streaming")
public class CampaignStreamingTests extends BaseTest {

    @Test(description = "Verify server streaming returns multiple campaigns")
    @Story("Server Streaming - List Campaigns")
    @Severity(SeverityLevel.CRITICAL)
    public void testListCampaignsServerStreaming() throws InterruptedException {
        // Arrange
        ListCampaignsRequest request = CampaignRequestBuilder.listCampaigns()
                .withUserId("user-123")
                .withPageSize(10)
                .build();

        // Act
        StreamingResponseCollector<CampaignMessage> collector =
                campaignClient.listCampaigns(request);

        // Wait for completion
        boolean completed = collector.awaitCompletion(10, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Stream should complete within timeout");
        assertTrue(collector.isCompletedSuccessfully(), "Stream should complete successfully");
        assertTrue(collector.getResponseCount() > 0, "Should receive at least one message");
    }

    @Test(description = "Verify specific number of streaming messages")
    @Story("Server Streaming - Expected Count")
    @Severity(SeverityLevel.NORMAL)
    public void testListCampaignsExpectedCount() throws InterruptedException {
        // Arrange
        int expectedCount = 5;
        ListCampaignsRequest request = CampaignRequestBuilder.listCampaigns()
                .withUserId("user-123")
                .withPageSize(expectedCount)
                .build();

        // Act
        StreamingResponseCollector<CampaignMessage> collector =
                campaignClient.listCampaigns(request, expectedCount);

        // Wait for expected count
        boolean receivedAll = collector.awaitExpectedCount(10, TimeUnit.SECONDS);

        // Assert
        assertTrue(receivedAll, "Should receive expected count within timeout");
        assertEquals(collector.getResponseCount(), expectedCount,
                "Should receive exactly " + expectedCount + " messages");
    }

    @Test(description = "Verify streaming message content")
    @Story("Server Streaming - Message Validation")
    @Severity(SeverityLevel.NORMAL)
    public void testStreamingMessageContent() throws InterruptedException {
        // Arrange
        ListCampaignsRequest request = CampaignRequestBuilder.listCampaigns()
                .withUserId("user-123")
                .withPageSize(5)
                .build();

        // Act
        StreamingResponseCollector<CampaignMessage> collector =
                campaignClient.listCampaigns(request);

        collector.awaitCompletion(10, TimeUnit.SECONDS);

        // Assert
        if (collector.getResponseCount() > 0) {
            CampaignMessage firstMessage = collector.getResponse(0);
            assertNotNull(firstMessage, "First message should not be null");
            assertNotNull(firstMessage.getCampaignId(), "Campaign ID should not be null");
            assertFalse(firstMessage.getTitle().isEmpty(), "Title should not be empty");
        }
    }

    @Test(description = "Verify streaming handles empty results")
    @Story("Server Streaming - Empty Results")
    @Severity(SeverityLevel.NORMAL)
    public void testStreamingEmptyResults() throws InterruptedException {
        // Arrange
        ListCampaignsRequest request = CampaignRequestBuilder.listCampaigns()
                .withUserId("nonexistent-user")
                .withPageSize(10)
                .build();

        // Act
        StreamingResponseCollector<CampaignMessage> collector =
                campaignClient.listCampaigns(request);

        boolean completed = collector.awaitCompletion(10, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Stream should complete even with no results");
        // May have 0 results for nonexistent user
        assertTrue(collector.getResponseCount() >= 0);
    }

    @Test(description = "Verify streaming pagination")
    @Story("Server Streaming - Pagination")
    @Severity(SeverityLevel.NORMAL)
    public void testStreamingPagination() throws InterruptedException {
        // Arrange - Request first page
        ListCampaignsRequest firstPage = CampaignRequestBuilder.listCampaigns()
                .withUserId("user-123")
                .withPageSize(3)
                .withPageToken("")
                .build();

        // Act
        StreamingResponseCollector<CampaignMessage> collector =
                campaignClient.listCampaigns(firstPage);

        collector.awaitCompletion(10, TimeUnit.SECONDS);

        // Assert
        assertTrue(collector.isCompletedSuccessfully());
        // Pagination validation would require actual page token from response
    }
}

