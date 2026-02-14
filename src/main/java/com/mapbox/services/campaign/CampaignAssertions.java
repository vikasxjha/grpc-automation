package com.mapbox.services.campaign;

import com.mapbox.core.assertions.GrpcResponse;
import com.mapbox.grpc.campaign.CampaignStatus;
import com.mapbox.grpc.campaign.GetCampaignResponse;
import io.grpc.Status;
import org.assertj.core.api.AbstractAssert;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom assertions for Campaign service responses
 * Provides domain-specific validation methods
 */
public class CampaignAssertions {

    private CampaignAssertions() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Assert that response contains valid campaign
     */
    public static void assertValidCampaign(GrpcResponse<GetCampaignResponse> response) {
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getCampaignId()).isNotEmpty();
    }

    /**
     * Assert campaign has specific ID
     */
    public static void assertCampaignId(
            GrpcResponse<GetCampaignResponse> response,
            String expectedId) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getCampaignId()).isEqualTo(expectedId);
    }

    /**
     * Assert campaign has specific status
     */
    public static void assertCampaignStatus(
            GrpcResponse<GetCampaignResponse> response,
            CampaignStatus expectedStatus) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    /**
     * Assert campaign title
     */
    public static void assertCampaignTitle(
            GrpcResponse<GetCampaignResponse> response,
            String expectedTitle) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getTitle()).isEqualTo(expectedTitle);
    }

    /**
     * Assert campaign subtitle
     */
    public static void assertCampaignSubtitle(
            GrpcResponse<GetCampaignResponse> response,
            String expectedSubtitle) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getSubtitle()).isEqualTo(expectedSubtitle);
    }

    /**
     * Assert campaign has specific tag
     */
    public static void assertHasTag(
            GrpcResponse<GetCampaignResponse> response,
            String tag) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getTagsList()).contains(tag);
    }

    /**
     * Assert campaign has all tags
     */
    public static void assertHasTags(
            GrpcResponse<GetCampaignResponse> response,
            List<String> expectedTags) {

        assertValidCampaign(response);
        assertThat(response.getResponse().getTagsList()).containsAll(expectedTags);
    }

    /**
     * Assert campaign property value
     */
    public static void assertPropertyValue(
            GrpcResponse<GetCampaignResponse> response,
            String key,
            String expectedValue) {

        assertValidCampaign(response);
        Map<String, String> properties = response.getResponse().getPropertiesMap();
        assertThat(properties).containsKey(key);
        assertThat(properties.get(key)).isEqualTo(expectedValue);
    }

    /**
     * Assert response has error status
     */
    public static void assertErrorStatus(
            GrpcResponse<GetCampaignResponse> response,
            Status.Code expectedCode) {

        assertThat(response).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo(expectedCode);
    }

    /**
     * Assert response latency
     */
    public static void assertLatencyWithin(
            GrpcResponse<GetCampaignResponse> response,
            long maxLatencyMs) {

        assertThat(response).isNotNull();
        assertThat(response.getLatencyMs())
                .isLessThanOrEqualTo(maxLatencyMs)
                .as("Response latency should be within %dms", maxLatencyMs);
    }

    /**
     * Create fluent assertion for campaign response
     */
    public static CampaignResponseAssert assertThatCampaign(
            GrpcResponse<GetCampaignResponse> response) {
        return new CampaignResponseAssert(response);
    }

    /**
     * Fluent assertion class for campaign responses
     */
    public static class CampaignResponseAssert
            extends AbstractAssert<CampaignResponseAssert, GrpcResponse<GetCampaignResponse>> {

        public CampaignResponseAssert(GrpcResponse<GetCampaignResponse> actual) {
            super(actual, CampaignResponseAssert.class);
        }

        public CampaignResponseAssert hasSuccessStatus() {
            isNotNull();
            if (!actual.isSuccess()) {
                failWithMessage("Expected success status but got: %s",
                        actual.getStatus().getCode());
            }
            return this;
        }

        public CampaignResponseAssert hasCampaignId(String expectedId) {
            hasSuccessStatus();
            if (!actual.getResponse().getCampaignId().equals(expectedId)) {
                failWithMessage("Expected campaign ID <%s> but was <%s>",
                        expectedId, actual.getResponse().getCampaignId());
            }
            return this;
        }

        public CampaignResponseAssert hasStatus(CampaignStatus expectedStatus) {
            hasSuccessStatus();
            if (actual.getResponse().getStatus() != expectedStatus) {
                failWithMessage("Expected status <%s> but was <%s>",
                        expectedStatus, actual.getResponse().getStatus());
            }
            return this;
        }

        public CampaignResponseAssert hasTitle(String expectedTitle) {
            hasSuccessStatus();
            if (!actual.getResponse().getTitle().equals(expectedTitle)) {
                failWithMessage("Expected title <%s> but was <%s>",
                        expectedTitle, actual.getResponse().getTitle());
            }
            return this;
        }

        public CampaignResponseAssert hasSubtitle(String expectedSubtitle) {
            hasSuccessStatus();
            if (!actual.getResponse().getSubtitle().equals(expectedSubtitle)) {
                failWithMessage("Expected subtitle <%s> but was <%s>",
                        expectedSubtitle, actual.getResponse().getSubtitle());
            }
            return this;
        }

        public CampaignResponseAssert latencyLessThan(long maxMs) {
            isNotNull();
            if (actual.getLatencyMs() >= maxMs) {
                failWithMessage("Expected latency < %dms but was %dms",
                        maxMs, actual.getLatencyMs());
            }
            return this;
        }
    }
}

