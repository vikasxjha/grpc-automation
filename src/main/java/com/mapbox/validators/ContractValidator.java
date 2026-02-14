package com.mapbox.validators;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contract validator for protobuf backward compatibility
 * Detects breaking changes in proto schemas
 */
@Slf4j
public class ContractValidator {

    /**
     * Validate backward compatibility between two proto schemas
     */
    public ValidationResult validateBackwardCompatibility(
            Descriptors.Descriptor baseline,
            Descriptors.Descriptor current) {

        ValidationResult result = new ValidationResult();

        // Check for removed fields
        checkRemovedFields(baseline, current, result);

        // Check for type changes
        checkFieldTypeChanges(baseline, current, result);

        // Check for optional to required changes
        checkRequiredFieldChanges(baseline, current, result);

        // Check for enum value removals
        checkEnumChanges(baseline, current, result);

        return result;
    }

    /**
     * Check for removed fields
     */
    private void checkRemovedFields(
            Descriptors.Descriptor baseline,
            Descriptors.Descriptor current,
            ValidationResult result) {

        Set<String> baselineFields = baseline.getFields().stream()
                .map(Descriptors.FieldDescriptor::getName)
                .collect(Collectors.toSet());

        Set<String> currentFields = current.getFields().stream()
                .map(Descriptors.FieldDescriptor::getName)
                .collect(Collectors.toSet());

        Set<String> removedFields = new HashSet<>(baselineFields);
        removedFields.removeAll(currentFields);

        if (!removedFields.isEmpty()) {
            result.addBreakingChange(
                    "Removed fields detected: " + String.join(", ", removedFields));
        }
    }

    /**
     * Check for field type changes
     */
    private void checkFieldTypeChanges(
            Descriptors.Descriptor baseline,
            Descriptors.Descriptor current,
            ValidationResult result) {

        Map<String, Descriptors.FieldDescriptor> baselineFieldMap = baseline.getFields().stream()
                .collect(Collectors.toMap(
                        Descriptors.FieldDescriptor::getName,
                        f -> f));

        Map<String, Descriptors.FieldDescriptor> currentFieldMap = current.getFields().stream()
                .collect(Collectors.toMap(
                        Descriptors.FieldDescriptor::getName,
                        f -> f));

        for (String fieldName : baselineFieldMap.keySet()) {
            if (currentFieldMap.containsKey(fieldName)) {
                Descriptors.FieldDescriptor baselineField = baselineFieldMap.get(fieldName);
                Descriptors.FieldDescriptor currentField = currentFieldMap.get(fieldName);

                if (baselineField.getType() != currentField.getType()) {
                    result.addBreakingChange(
                            String.format("Field '%s' type changed from %s to %s",
                                    fieldName,
                                    baselineField.getType(),
                                    currentField.getType()));
                }
            }
        }
    }

    /**
     * Check for optional to required field changes
     */
    private void checkRequiredFieldChanges(
            Descriptors.Descriptor baseline,
            Descriptors.Descriptor current,
            ValidationResult result) {

        Map<String, Descriptors.FieldDescriptor> baselineFieldMap = baseline.getFields().stream()
                .collect(Collectors.toMap(
                        Descriptors.FieldDescriptor::getName,
                        f -> f));

        Map<String, Descriptors.FieldDescriptor> currentFieldMap = current.getFields().stream()
                .collect(Collectors.toMap(
                        Descriptors.FieldDescriptor::getName,
                        f -> f));

        for (String fieldName : currentFieldMap.keySet()) {
            if (baselineFieldMap.containsKey(fieldName)) {
                Descriptors.FieldDescriptor baselineField = baselineFieldMap.get(fieldName);
                Descriptors.FieldDescriptor currentField = currentFieldMap.get(fieldName);

                if (!baselineField.isRequired() && currentField.isRequired()) {
                    result.addBreakingChange(
                            String.format("Field '%s' changed from optional to required", fieldName));
                }
            }
        }
    }

    /**
     * Check for enum changes
     */
    private void checkEnumChanges(
            Descriptors.Descriptor baseline,
            Descriptors.Descriptor current,
            ValidationResult result) {

        // Check enum fields
        for (Descriptors.FieldDescriptor field : baseline.getFields()) {
            if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM) {
                Descriptors.FieldDescriptor currentField = current.findFieldByName(field.getName());
                if (currentField != null && currentField.getType() == Descriptors.FieldDescriptor.Type.ENUM) {
                    checkEnumValueChanges(field.getEnumType(), currentField.getEnumType(), result);
                }
            }
        }
    }

    /**
     * Check enum value changes
     */
    private void checkEnumValueChanges(
            Descriptors.EnumDescriptor baseline,
            Descriptors.EnumDescriptor current,
            ValidationResult result) {

        Set<String> baselineValues = baseline.getValues().stream()
                .map(Descriptors.EnumValueDescriptor::getName)
                .collect(Collectors.toSet());

        Set<String> currentValues = current.getValues().stream()
                .map(Descriptors.EnumValueDescriptor::getName)
                .collect(Collectors.toSet());

        Set<String> removedValues = new HashSet<>(baselineValues);
        removedValues.removeAll(currentValues);

        if (!removedValues.isEmpty()) {
            result.addBreakingChange(
                    String.format("Enum '%s' removed values: %s",
                            baseline.getName(),
                            String.join(", ", removedValues)));
        }
    }

    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final List<String> breakingChanges = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public void addBreakingChange(String change) {
            breakingChanges.add(change);
            log.error("Breaking change detected: {}", change);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
            log.warn("Warning: {}", warning);
        }

        public boolean isCompatible() {
            return breakingChanges.isEmpty();
        }

        public List<String> getBreakingChanges() {
            return Collections.unmodifiableList(breakingChanges);
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result:\n");
            sb.append("Compatible: ").append(isCompatible()).append("\n");

            if (!breakingChanges.isEmpty()) {
                sb.append("\nBreaking Changes:\n");
                breakingChanges.forEach(change -> sb.append("  - ").append(change).append("\n"));
            }

            if (!warnings.isEmpty()) {
                sb.append("\nWarnings:\n");
                warnings.forEach(warning -> sb.append("  - ").append(warning).append("\n"));
            }

            return sb.toString();
        }
    }
}

