package com.lilyai.producttypedetection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductDto(
    @NotNull(message = "Style ID cannot be null")
    @Size(min = 1, max = 50, message = "Style ID must be between 1 and 50 characters")
    String styleId,
    
    @NotNull(message = "SKU ID cannot be null")
    @Size(min = 1, max = 50, message = "SKU ID must be between 1 and 50 characters")
    String skuId,
    
    @NotNull(message = "Title cannot be null")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,
    
    @NotNull(message = "Brand cannot be null")
    @Size(min = 1, max = 100, message = "Brand must be between 1 and 100 characters")
    String brand,
    
    @NotNull(message = "Image URLs cannot be null")
    @Size(min = 1, max = 10, message = "Must have between 1 and 10 image URLs")
    List<@NotNull @Size(min = 1, max = 500, message = "Image URL must be between 1 and 500 characters") String> imageUrls
) {}