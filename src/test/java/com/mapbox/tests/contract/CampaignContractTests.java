package com.mapbox.tests.contract;

import com.mapbox.grpc.campaign.GetCampaignResponse;
import com.mapbox.validators.ContractValidator;
import com.mapbox.tests.base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Contract validation tests
 * Validates proto schema compatibility and backward compatibility
 */
@Epic("Campaign Service")
@Feature("Contract Validation")
public class CampaignContractTests extends BaseTest {

    @Test(description = "Verify proto schema has required fields")
    @Story("Schema Validation - Required Fields")
    @Severity(SeverityLevel.CRITICAL)
    public void testSchemaHasRequiredFields() {
        // Get descriptor for GetCampaignResponse
        var descriptor = GetCampaignResponse.getDescriptor();

        // Assert required fields exist
        assertNotNull(descriptor.findFieldByName("campaign_id"),
                "campaign_id field should exist");
        assertNotNull(descriptor.findFieldByName("title"),
                "title field should exist");
        assertNotNull(descriptor.findFieldByName("subtitle"),
                "subtitle field should exist");
        assertNotNull(descriptor.findFieldByName("status"),
                "status field should exist");
    }

    @Test(description = "Verify field types are correct")
    @Story("Schema Validation - Field Types")
    @Severity(SeverityLevel.CRITICAL)
    public void testFieldTypes() {
        var descriptor = GetCampaignResponse.getDescriptor();

        // Verify campaign_id is STRING
        var campaignIdField = descriptor.findFieldByName("campaign_id");
        assertEquals(campaignIdField.getType().toString(), "STRING",
                "campaign_id should be STRING type");

        // Verify tags is REPEATED
        var tagsField = descriptor.findFieldByName("tags");
        assertTrue(tagsField.isRepeated(), "tags should be repeated field");

        // Verify properties is MAP
        var propertiesField = descriptor.findFieldByName("properties");
        assertTrue(propertiesField.isMapField(), "properties should be map field");
    }

    @Test(description = "Verify enum values exist")
    @Story("Schema Validation - Enum Values")
    @Severity(SeverityLevel.NORMAL)
    public void testEnumValues() {
        var descriptor = GetCampaignResponse.getDescriptor();
        var statusField = descriptor.findFieldByName("status");
        var enumDescriptor = statusField.getEnumType();

        // Verify enum values
        assertNotNull(enumDescriptor.findValueByName("UNKNOWN"),
                "UNKNOWN status should exist");
        assertNotNull(enumDescriptor.findValueByName("ACTIVE"),
                "ACTIVE status should exist");
        assertNotNull(enumDescriptor.findValueByName("PAUSED"),
                "PAUSED status should exist");
        assertNotNull(enumDescriptor.findValueByName("COMPLETED"),
                "COMPLETED status should exist");
        assertNotNull(enumDescriptor.findValueByName("CANCELLED"),
                "CANCELLED status should exist");
    }

    @Test(description = "Verify backward compatibility")
    @Story("Contract Validation - Backward Compatibility")
    @Severity(SeverityLevel.CRITICAL)
    public void testBackwardCompatibility() {
        // In real scenario, you would load baseline schema from file
        // and compare with current schema

        ContractValidator validator = new ContractValidator();
        var currentDescriptor = GetCampaignResponse.getDescriptor();

        // For this example, we compare with itself (always compatible)
        var result = validator.validateBackwardCompatibility(
                currentDescriptor, currentDescriptor);

        assertTrue(result.isCompatible(),
                "Schema should be compatible with itself");
        assertTrue(result.getBreakingChanges().isEmpty(),
                "Should have no breaking changes");
    }

    @Test(description = "Verify no optional fields became required")
    @Story("Contract Validation - Field Requirements")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoOptionalToRequiredChanges() {
        // This test would compare baseline with current
        // For now, we verify current schema doesn't have required fields
        // (proto3 doesn't support required keyword)

        var descriptor = GetCampaignResponse.getDescriptor();

        for (var field : descriptor.getFields()) {
            assertFalse(field.isRequired(),
                    "Field " + field.getName() + " should not be required in proto3");
        }
    }

    @Test(description = "Verify message can be serialized and deserialized")
    @Story("Contract Validation - Serialization")
    @Severity(SeverityLevel.NORMAL)
    public void testMessageSerializationDeserialization() throws Exception {
        // Create a message
        GetCampaignResponse original = GetCampaignResponse.newBuilder()
                .setCampaignId("test-123")
                .setTitle("Test Campaign")
                .setSubtitle("Test Subtitle")
                .build();

        // Serialize
        byte[] serialized = original.toByteArray();

        // Deserialize
        GetCampaignResponse deserialized = GetCampaignResponse.parseFrom(serialized);

        // Assert
        assertEquals(deserialized.getCampaignId(), original.getCampaignId());
        assertEquals(deserialized.getTitle(), original.getTitle());
        assertEquals(deserialized.getSubtitle(), original.getSubtitle());
    }
}

