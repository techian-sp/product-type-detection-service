package com.lilyai.producttypedetection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BatchResultDto(
    @JsonProperty("success_count")
    @NotNull
    @Min(0)
    Integer successCount,
    
    @JsonProperty("duplicate_count")
    @NotNull
    @Min(0)
    Integer duplicateCount,
    
    @JsonProperty("failed_count")
    @NotNull
    @Min(0)
    Integer failedCount,
    
    @JsonProperty("error_messages")
    @NotNull
    List<String> errorMessages
) {
    
    public BatchResultDto {
        if (successCount < 0) {
            throw new IllegalArgumentException("Success count cannot be negative");
        }
        if (duplicateCount < 0) {
            throw new IllegalArgumentException("Duplicate count cannot be negative");
        }
        if (failedCount < 0) {
            throw new IllegalArgumentException("Failed count cannot be negative");
        }
        errorMessages = List.copyOf(errorMessages);
    }
    
    public int getTotalProcessed() {
        return successCount + duplicateCount + failedCount;
    }
    
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
    
    public boolean isSuccessful() {
        return failedCount == 0 && errorMessages.isEmpty();
    }
    
    public static BatchResultDto success(int successCount, int duplicateCount) {
        return new BatchResultDto(successCount, duplicateCount, 0, List.of());
    }
    
    public static BatchResultDto withErrors(int successCount, int duplicateCount, int failedCount, List<String> errorMessages) {
        return new BatchResultDto(successCount, duplicateCount, failedCount, errorMessages);
    }
}