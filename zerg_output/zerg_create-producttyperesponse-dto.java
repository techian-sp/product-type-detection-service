```java
package com.example.detection.dto;

import java.util.Objects;

public record DetectionResponseDTO(ProductType productType) {

    public DetectionResponseDTO {
        Objects.requireNonNull(productType, "productType must not be null");
    }

    public enum ProductType {
        ELECTRONICS, CLOTHING, FOOD, OTHER;

        public static ProductType fromString(String type) {
            return switch (type.toUpperCase()) {
                case "ELECTRONICS" -> ELECTRONICS;
                case "CLOTHING" -> CLOTHING;
                case "FOOD" -> FOOD;
                default -> OTHER;
            };
        }
    }
}
```