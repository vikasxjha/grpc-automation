package com.mapbox.testdata.campaign;

import com.mapbox.grpc.campaign.GetCampaignRequest;
import com.mapbox.grpc.campaign.ListCampaignsRequest;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Request builder for Campaign service
 * Provides fluent API for building test requests
 */
public class CampaignRequestBuilder {

    /**
     * Builder for GetCampaignRequest
     */
    public static class GetCampaignRequestBuilder {
        private String caller;
        private String receiver;
        private String campaignId;
        private Map<String, String> metadata = new HashMap<>();

        public GetCampaignRequestBuilder withCaller(String caller) {
            this.caller = caller;
            return this;
        }

        public GetCampaignRequestBuilder withReceiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public GetCampaignRequestBuilder withCampaignId(String campaignId) {
            this.campaignId = campaignId;
            return this;
        }

        public GetCampaignRequestBuilder withMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public GetCampaignRequestBuilder withMetadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public GetCampaignRequest build() {
            return GetCampaignRequest.newBuilder()
                    .setCaller(caller != null ? caller : "")
                    .setReceiver(receiver != null ? receiver : "")
                    .setCampaignId(campaignId != null ? campaignId : "")
                    .putAllMetadata(metadata)
                    .build();
        }
    }

    /**
     * Builder for ListCampaignsRequest
     */
    public static class ListCampaignsRequestBuilder {
        private String userId;
        private int pageSize = 10;
        private String pageToken = "";

        public ListCampaignsRequestBuilder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public ListCampaignsRequestBuilder withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public ListCampaignsRequestBuilder withPageToken(String pageToken) {
            this.pageToken = pageToken;
            return this;
        }

        public ListCampaignsRequest build() {
            return ListCampaignsRequest.newBuilder()
                    .setUserId(userId != null ? userId : "")
                    .setPageSize(pageSize)
                    .setPageToken(pageToken)
                    .build();
        }
    }

    public static GetCampaignRequestBuilder getCampaign() {
        return new GetCampaignRequestBuilder();
    }

    public static ListCampaignsRequestBuilder listCampaigns() {
        return new ListCampaignsRequestBuilder();
    }
}

