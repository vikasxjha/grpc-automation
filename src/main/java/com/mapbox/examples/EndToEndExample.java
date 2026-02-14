package com.mapbox.examples;

import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.core.channel.GrpcChannelFactory;
import com.mapbox.grpc.campaign.CampaignStatus;
import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.services.campaign.CampaignClient;
import com.mapbox.testdata.campaign.CampaignRequestBuilder;
import lombok.extern.slf4j.Slf4j;

import static com.mapbox.core.assertions.GrpcResponseAssert.assertThat;
import static com.mapbox.services.campaign.CampaignAssertions.assertThatCampaign;

/**
 * Complete end-to-end example demonstrating framework usage
 * This is NOT a test - it's a reference implementation
 */
@Slf4j
public class EndToEndExample {

    public static void main(String[] args) {
        log.info("=== gRPC Framework End-to-End Example ===\n");

        // Step 1: Initialize the framework
        log.info("Step 1: Initializing framework components...");
        GrpcChannelFactory channelFactory = new GrpcChannelFactory();
        CampaignClient campaignClient = new CampaignClient(channelFactory);

        try {
            // Step 2: Build a request using the fluent builder
            log.info("\nStep 2: Building request...");
            GetCampaignRequest request = CampaignRequestBuilder.getCampaign()
                    .withCaller("919999999999")
                    .withReceiver("918888888888")
                    .withCampaignId("campaign-123")
                    .withMetadata("source", "example")
                    .withMetadata("version", "1.0")
                    .build();

            log.info("Request built: caller={}, receiver={}, campaignId={}",
                    request.getCaller(), request.getReceiver(), request.getCampaignId());

            // Step 3: Execute the gRPC call
            log.info("\nStep 3: Executing gRPC call...");
            GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

            // Step 4: Validate using custom assertions
            log.info("\nStep 4: Validating response...");

            // Generic assertion style
            assertThat(response)
                    .hasStatusOk()
                    .hasField("campaign_id", "campaign-123")
                    .latencyLessThan(500);

            log.info("✓ Generic assertions passed");

            // Domain-specific fluent assertion style
            assertThatCampaign(response)
                    .hasSuccessStatus()
                    .hasCampaignId("campaign-123")
                    .hasStatus(CampaignStatus.ACTIVE)
                    .hasSubtitle("Test Subtitle");

            log.info("✓ Domain-specific assertions passed");

            // Step 5: Access response data
            log.info("\nStep 5: Accessing response data...");
            if (response.isSuccess()) {
                GetCampaignResponse campaignResponse = response.getResponse();

                log.info("Campaign Details:");
                log.info("  - Campaign ID: {}", campaignResponse.getCampaignId());
                log.info("  - Title: {}", campaignResponse.getTitle());
                log.info("  - Subtitle: {}", campaignResponse.getSubtitle());
                log.info("  - Status: {}", campaignResponse.getStatus());
                log.info("  - Tags: {}", campaignResponse.getTagsList());
                log.info("  - Properties: {}", campaignResponse.getPropertiesMap());
                log.info("  - Latency: {}ms", response.getLatencyMs());
            }

            // Step 6: Demonstrate error handling
            log.info("\nStep 6: Demonstrating error handling...");
            GetCampaignRequest invalidRequest = CampaignRequestBuilder.getCampaign()
                    .withCaller("invalid")
                    .withReceiver("918888888888")
                    .withCampaignId("campaign-123")
                    .build();

            GrpcResponse<GetCampaignResponse> errorResponse =
                    campaignClient.getCampaign(invalidRequest);

            if (!errorResponse.isSuccess()) {
                log.info("Expected error received:");
                log.info("  - Status Code: {}", errorResponse.getStatus().getCode());
                log.info("  - Description: {}", errorResponse.getStatus().getDescription());
            }

            log.info("\n=== Example Completed Successfully ===");

        } catch (Exception e) {
            log.error("Example failed with error", e);
        } finally {
            // Step 7: Cleanup
            log.info("\nStep 7: Cleaning up...");
            channelFactory.shutdownAll();
            log.info("✓ Resources released");
        }
    }
}

