package com.lilyai.producttypedetection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Response DTO for product type detection endpoint.
 * Contains the detected product type information.
 */
public record ProductTypeDetectionResponse(
    @JsonProperty("product_type")
    @NotBlank(message = "Product type cannot be blank")
    String productType
) implements Serializable {
    
    /**
     * Creates a new ProductTypeDetectionResponse with the specified product type.
     * 
     * @param productType the detected product type, must not be blank
     * @throws IllegalArgumentException if productType is null or blank
     */
    public ProductTypeDetectionResponse {
        if (productType == null || productType.isBlank()) {
            throw new IllegalArgumentException("Product type cannot be null or blank");
        }
        productType = productType.trim();
    }
    
    /**
     * Factory method to create a response with the detected product type.
     * 
     * @param detectedType the detected product type
     * @return new ProductTypeDetectionResponse instance
     */
    public static ProductTypeDetectionResponse of(String detectedType) {
        return new ProductTypeDetectionResponse(detectedType);
    }
}