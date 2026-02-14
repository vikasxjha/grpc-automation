package com.mapbox.tests.functional;

import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.grpc.campaign.CampaignStatus;
import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.services.campaign.CampaignAssertions;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import com.mapbox.testdata.campaign.CampaignTestData;
import com.mapbox.tests.base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static com.mapbox.core.assertions.GrpcResponseAssert.assertThat;
import static com.mapbox.services.campaign.CampaignAssertions.*;

/**
 * Functional tests for Campaign Service
 */
@Epic("Campaign Service")
@Feature("Get Campaign")
public class CampaignFunctionalTests extends BaseTest {

    @Test(description = "Verify campaign can be retrieved with valid parameters")
    @Story("Get Campaign - Happy Path")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetCampaignSuccess() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertThat(response)
                .hasStatusOk()
                .latencyLessThan(200);

        assertValidCampaign(response);
        assertCampaignId(response, CampaignTestData.CampaignIds.VALID_CAMPAIGN);
    }

    @Test(description = "Verify campaign subtitle is returned correctly")
    @Story("Get Campaign - Field Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetCampaignSubtitle() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert - Using custom fluent assertion
        assertThatCampaign(response)
                .hasSuccessStatus()
                .hasSubtitle("Test Subtitle")
                .latencyLessThan(300);
    }

    @Test(description = "Verify campaign with metadata")
    @Story("Get Campaign - Metadata")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCampaignWithMetadata() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .withMetadata("source", "automation")
                .withMetadata("environment", "test")
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertThat(response).hasStatusOk();
        assertPropertyValue(response, "priority", "high");
    }

    @Test(description = "Verify campaign status is ACTIVE")
    @Story("Get Campaign - Status Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetCampaignActiveStatus() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertThatCampaign(response)
                .hasSuccessStatus()
                .hasStatus(CampaignStatus.ACTIVE);
    }

    @Test(description = "Verify campaign tags are returned")
    @Story("Get Campaign - Tags")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCampaignTags() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertValidCampaign(response);
        assertHasTag(response, "test");
        assertHasTag(response, "automation");
    }

    @Test(description = "Verify response time is acceptable")
    @Story("Get Campaign - Performance")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCampaignPerformance() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertLatencyWithin(response, 500);
    }
}

