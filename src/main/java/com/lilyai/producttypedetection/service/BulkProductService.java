package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.entity.Product;
import com.lilyai.producttypedetection.repository.ProductRepository;
import com.lilyai.producttypedetection.dto.ProductDto;
import com.lilyai.producttypedetection.dto.BulkProductResult;
import com.lilyai.producttypedetection.exception.BulkProductException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@Validated
public class BulkProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(BulkProductService.class);
    private static final int MIN_BULK_SIZE = 30;
    
    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;
    private final Timer bulkInsertTimer;
    private final Counter validationErrorCounter;
    private final Counter duplicateCounter;
    private final Counter successCounter;
    
    public BulkProductService(ProductRepository productRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.meterRegistry = meterRegistry;
        this.bulkInsertTimer = Timer.builder("bulk.product.insert.duration")
            .description("Time taken for bulk product insertion")
            .register(meterRegistry);
        this.validationErrorCounter = Counter.builder("bulk.product.validation.errors")
            .description("Number of validation errors during bulk insert")
            .register(meterRegistry);
        this.duplicateCounter = Counter.builder("bulk.product.duplicates")
            .description("Number of duplicate products found")
            .register(meterRegistry);
        this.successCounter = Counter.builder("bulk.product.success")
            .description("Number of successfully inserted products")
            .register(meterRegistry);
    }
    
    @Transactional
    public BulkProductResult processBulkProducts(
            @NotEmpty @Size(min = MIN_BULK_SIZE, message = "Minimum 30 products required for bulk processing")
            List<@Valid ProductDto> productDtos) {
        
        var startTime = Instant.now();
        logger.info("Starting bulk product processing for {} products", productDtos.size());
        
        return bulkInsertTimer.recordCallable(() -> {
            try {
                // Validate input size
                if (productDtos.size() < MIN_BULK_SIZE) {
                    throw new BulkProductException(STR."Minimum \{MIN_BULK_SIZE} products required, received: \{productDtos.size()}");
                }
                
                // Step 1: Validate products
                var validationResult = validateProducts(productDtos);
                var validProducts = validationResult.validProducts();
                var validationErrors = validationResult.errors();
                
                validationErrorCounter.increment(validationErrors.size());
                
                if (validProducts.isEmpty()) {
                    logger.warn("No valid products found after validation");
                    return new BulkProductResult(0, 0, validationErrors.size(), validationErrors, List.of());
                }
                
                // Step 2: Deduplicate products
                var deduplicationResult = deduplicateProducts(validProducts);
                var uniqueProducts = deduplicationResult.uniqueProducts();
                var duplicateCount = deduplicationResult.duplicateCount();
                
                duplicateCounter.increment(duplicateCount);
                
                // Step 3: Check for existing products in database
                var existingProductsResult = filterExistingProducts(uniqueProducts);
                var newProducts = existingProductsResult.newProducts();
                var existingCount = existingProductsResult.existingCount();
                
                // Step 4: Bulk insert new products
                var insertedProducts = insertProducts(newProducts);
                var successCount = insertedProducts.size();
                
                successCounter.increment(successCount);
                
                var processingTime = java.time.Duration.between(startTime, Instant.now());
                logger.info("Bulk processing completed: {} inserted, {} duplicates, {} validation errors, {} existing products. Processing time: {}ms",
                    successCount, duplicateCount, validationErrors.size(), existingCount, processingTime.toMillis());
                
                return new BulkProductResult(
                    successCount,
                    duplicateCount + existingCount,
                    validationErrors.size(),
                    validationErrors,
                    insertedProducts
                );
                
            } catch (Exception e) {
                logger.error("Error during bulk product processing", e);
                throw new BulkProductException("Failed to process bulk products", e);
            }
        });
    }
    
    private ValidationResult validateProducts(List<ProductDto> productDtos) {
        var validProducts = new ArrayList<ProductDto>();
        var errors = new ArrayList<String>();
        
        for (int i = 0; i < productDtos.size(); i++) {
            var product = productDtos.get(i);
            var validationErrors = validateSingleProduct(product, i);
            
            if (validationErrors.isEmpty()) {
                validProducts.add(product);
            } else {
                errors.addAll(validationErrors);
            }
        }
        
        return new ValidationResult(validProducts, errors);
    }
    
    private List<String> validateSingleProduct(ProductDto product, int index) {
        var errors = new ArrayList<String>();
        
        if (product.name() == null || product.name().trim().isEmpty()) {
            errors.add(STR."Product at index \{index}: name is required");
        }
        
        if (product.sku() == null || product.sku().trim().isEmpty()) {
            errors.add(STR."Product at index \{index}: SKU is required");
        }
        
        if (product.price() == null || product.price().compareTo(java.math.BigDecimal.ZERO) < 0) {
            errors.add(STR."Product at index \{index}: price must be non-negative");
        }
        
        return errors;
    }
    
    private DeduplicationResult deduplicateProducts(List<ProductDto> products) {
        var uniqueProductsMap = products.stream()
            .collect(Collectors.toMap(
                ProductDto::sku,
                Function.identity(),
                (existing, duplicate) -> existing,
                LinkedHashMap::new
            ));
        
        var duplicateCount = products.size() - uniqueProductsMap.size();
        var uniqueProducts = new ArrayList<>(uniqueProductsMap.values());
        
        logger.debug("Deduplication completed: {} unique products, {} duplicates removed", 
            uniqueProducts.size(), duplicateCount);
        
        return new DeduplicationResult(uniqueProducts, duplicateCount);
    }
    
    private ExistingProductsResult filterExistingProducts(List<ProductDto> products) {
        var skus = products.stream()
            .map(ProductDto::sku)
            .collect(Collectors.toSet());
        
        var existingSkus = productRepository.findExistingSkus(skus);
        
        var newProducts = products.stream()
            .filter(product -> !existingSkus.contains(product.sku()))
            .toList();
        
        var existingCount = products.size() - newProducts.size();
        
        logger.debug("Filtered existing products: {} new products, {} existing products", 
            newProducts.size(), existingCount);
        
        return new ExistingProductsResult(newProducts, existingCount);
    }
    
    private List<Product> insertProducts(List<ProductDto> productDtos) {
        if (productDtos.isEmpty()) {
            return List.of();
        }
        
        var products = productDtos.stream()
            .map(this::convertToEntity)
            .toList();
        
        var savedProducts = productRepository.saveAll(products);
        
        logger.info("Successfully inserted {} products", savedProducts.size());
        
        return savedProducts;
    }
    
    private Product convertToEntity(ProductDto dto) {
        var product = new Product();
        product.setName(dto.name().trim());
        product.setSku(dto.sku().trim().toUpperCase());
        product.setDescription(dto.description() != null ? dto.description().trim() : null);
        product.setPrice(dto.price());
        product.setCategory(dto.category());
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        return product;
    }
    
    private record ValidationResult(List<ProductDto> validProducts, List<String> errors) {}
    
    private record DeduplicationResult(List<ProductDto> uniqueProducts, int duplicateCount) {}
    
    private record ExistingProductsResult(List<ProductDto> newProducts, int existingCount) {}
}