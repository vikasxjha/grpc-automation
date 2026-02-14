package com.mapbox.tests.negative;

import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import com.mapbox.testdata.campaign.CampaignTestData;
import com.mapbox.tests.base.BaseTest;
import io.grpc.Status;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static com.mapbox.core.assertions.GrpcResponseAssert.assertThat;
import static com.mapbox.services.campaign.CampaignAssertions.assertErrorStatus;

/**
 * Negative tests for Campaign Service
 * Validates error handling and edge cases
 */
@Epic("Campaign Service")
@Feature("Negative Tests")
public class CampaignNegativeTests extends BaseTest {

    @Test(description = "Verify invalid phone number returns INVALID_ARGUMENT")
    @Story("Error Handling - Invalid Input")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidPhoneNumberReturnsInvalidArgument() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.INVALID_FORMAT)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertThat(response)
                .hasStatus(Status.Code.INVALID_ARGUMENT)
                .hasErrorDescriptionContaining("phone");
    }

    @Test(description = "Verify empty caller returns INVALID_ARGUMENT")
    @Story("Error Handling - Missing Required Field")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmptyCallerReturnsInvalidArgument() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.EMPTY)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertErrorStatus(response, Status.Code.INVALID_ARGUMENT);
    }

    @Test(description = "Verify non-existent campaign returns NOT_FOUND")
    @Story("Error Handling - Resource Not Found")
    @Severity(SeverityLevel.CRITICAL)
    public void testNonExistentCampaignReturnsNotFound() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.NON_EXISTENT)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertThat(response)
                .hasStatus(Status.Code.NOT_FOUND)
                .hasErrorDescriptionContaining("campaign");
    }

    @Test(description = "Verify empty campaign ID returns INVALID_ARGUMENT")
    @Story("Error Handling - Missing Campaign ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmptyCampaignIdReturnsInvalidArgument() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId("")
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertErrorStatus(response, Status.Code.INVALID_ARGUMENT);
    }

    @Test(description = "Verify malformed campaign ID returns INVALID_ARGUMENT")
    @Story("Error Handling - Invalid Format")
    @Severity(SeverityLevel.NORMAL)
    public void testMalformedCampaignIdReturnsInvalidArgument() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller(CampaignTestData.PhoneNumbers.VALID_CALLER)
                .withReceiver(CampaignTestData.PhoneNumbers.VALID_RECEIVER)
                .withCampaignId(CampaignTestData.CampaignIds.INVALID)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertErrorStatus(response, Status.Code.INVALID_ARGUMENT);
    }

    @Test(description = "Verify both phone numbers empty returns INVALID_ARGUMENT")
    @Story("Error Handling - Multiple Missing Fields")
    @Severity(SeverityLevel.NORMAL)
    public void testBothPhoneNumbersEmptyReturnsInvalidArgument() {
        // Arrange
        GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                .withCaller("")
                .withReceiver("")
                .withCampaignId(CampaignTestData.CampaignIds.VALID_CAMPAIGN)
                .build();

        // Act
        GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

        // Assert
        assertErrorStatus(response, Status.Code.INVALID_ARGUMENT);
    }
}

