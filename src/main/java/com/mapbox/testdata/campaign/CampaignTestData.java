package com.mapbox.testdata.campaign;

import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.grpc.campaign.CampaignStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test data fixtures for Campaign responses
 * Provides pre-built test data for various scenarios
 */
public class CampaignTestData {

    /**
     * Create a valid campaign response
     */
    public static GetCampaignResponse createValidCampaignResponse(String campaignId) {
        return GetCampaignResponse.newBuilder()
                .setCampaignId(campaignId)
                .setTitle("Test Campaign")
                .setSubtitle("Test Subtitle")
                .setDescription("This is a test campaign for automation")
                .setStatus(CampaignStatus.ACTIVE)
                .addAllTags(Arrays.asList("test", "automation", "grpc"))
                .putAllProperties(createDefaultProperties())
                .setCreatedAt(System.currentTimeMillis())
                .setUpdatedAt(System.currentTimeMillis())
                .build();
    }

    /**
     * Create campaign response with custom status
     */
    public static GetCampaignResponse createCampaignWithStatus(String campaignId, CampaignStatus status) {
        return GetCampaignResponse.newBuilder()
                .setCampaignId(campaignId)
                .setTitle("Test Campaign")
                .setSubtitle("Test Subtitle")
                .setDescription("Campaign with status: " + status)
                .setStatus(status)
                .setCreatedAt(System.currentTimeMillis())
                .setUpdatedAt(System.currentTimeMillis())
                .build();
    }

    /**
     * Create campaign response with custom properties
     */
    public static GetCampaignResponse createCampaignWithProperties(
            String campaignId,
            Map<String, String> properties) {
        return GetCampaignResponse.newBuilder()
                .setCampaignId(campaignId)
                .setTitle("Test Campaign")
                .setSubtitle("Test Subtitle")
                .setDescription("Campaign with custom properties")
                .setStatus(CampaignStatus.ACTIVE)
                .putAllProperties(properties)
                .setCreatedAt(System.currentTimeMillis())
                .setUpdatedAt(System.currentTimeMillis())
                .build();
    }

    /**
     * Default properties
     */
    private static Map<String, String> createDefaultProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("priority", "high");
        properties.put("channel", "sms");
        properties.put("region", "us-west");
        return properties;
    }

    /**
     * Common test phone numbers
     */
    public static class PhoneNumbers {
        public static final String VALID_CALLER = "919999999999";
        public static final String VALID_RECEIVER = "918888888888";
        public static final String INVALID_FORMAT = "invalid";
        public static final String EMPTY = "";
    }

    /**
     * Common test campaign IDs
     */
    public static class CampaignIds {
        public static final String VALID_CAMPAIGN = "campaign-123";
        public static final String NON_EXISTENT = "campaign-999";
        public static final String INVALID = "invalid-id";
    }
}

