package com.lilyai.producttypedetection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;
    
    @Column(name = "product_type", length = 100)
    private String productType;
    
    @Column(name = "category_id")
    private UUID categoryId;
    
    @Column(name = "brand_name", length = 100)
    private String brandName;
    
    @Column(name = "model_number", length = 100)
    private String modelNumber;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "weight_grams")
    private Double weightGrams;
    
    @Column(name = "dimensions_json", columnDefinition = "JSON")
    private String dimensionsJson;
    
    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;
    
    @Column(name = "image_urls_json", columnDefinition = "JSON")
    private String imageUrlsJson;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
}