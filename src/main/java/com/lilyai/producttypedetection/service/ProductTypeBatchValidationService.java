package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.model.ProductType;
import com.lilyai.producttypedetection.model.ValidationResult;
import com.lilyai.producttypedetection.repository.ProductRepository;
import com.lilyai.producttypedetection.repository.ProductTypeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductTypeBatchValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductTypeBatchValidationService.class);
    
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final Executor taskExecutor;
    private final Counter validationCounter;
    private final Counter errorCounter;
    private final Timer validationTimer;
    private final Timer repositoryTimer;
    
    @Value("${app.batch.size:100}")
    private int batchSize;
    
    @Value("${app.batch.max-concurrent:5}")
    private int maxConcurrentBatches;
    
    public ProductTypeBatchValidationService(
            ProductRepository productRepository,
            ProductTypeRepository productTypeRepository,
            Executor taskExecutor,
            MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.taskExecutor = taskExecutor;
        this.validationCounter = Counter.builder("product.validation.total")
                .description("Total product validations performed")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("product.validation.errors")
                .description("Total validation errors")
                .register(meterRegistry);
        this.validationTimer = Timer.builder("product.validation.duration")
                .description("Product validation duration")
                .register(meterRegistry);
        this.repositoryTimer = Timer.builder("product.repository.duration")
                .description("Repository operation duration")
                .register(meterRegistry);
    }
    
    @Transactional(readOnly = true)
    public CompletableFuture<List<ValidationResult>> validateProductsBatch(List<String> productIds) {
        var startTime = Instant.now();
        logger.info("Starting batch validation for {} products", productIds.size());
        
        if (CollectionUtils.isEmpty(productIds)) {
            logger.warn("Empty product IDs list provided for validation");
            return CompletableFuture.completedFuture(List.of());
        }
        
        return validationTimer.recordCallable(() -> {
            try {
                var batches = partitionIntoBatches(productIds, batchSize);
                logger.debug("Split {} products into {} batches of size {}", 
                    productIds.size(), batches.size(), batchSize);
                
                var futures = batches.stream()
                    .limit(maxConcurrentBatches)
                    .map(this::processBatch)
                    .toList();
                
                var results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(_ -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));
                
                return results.whenComplete((validationResults, throwable) -> {
                    var duration = Duration.between(startTime, Instant.now());
                    if (throwable != null) {
                        logger.error("Batch validation failed after {} ms", duration.toMillis(), throwable);
                        errorCounter.increment();
                    } else {
                        var successCount = validationResults.stream()
                            .mapToLong(result -> result.isValid() ? 1 : 0)
                            .sum();
                        logger.info("Completed batch validation: {}/{} valid products in {} ms", 
                            successCount, validationResults.size(), duration.toMillis());
                        validationCounter.increment(validationResults.size());
                    }
                }).join();
                
            } catch (Exception e) {
                logger.error("Unexpected error during batch validation", e);
                errorCounter.increment();
                throw new RuntimeException("Batch validation failed", e);
            }
        });
    }
    
    private CompletableFuture<List<ValidationResult>> processBatch(List<String> batch) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Processing batch of {} products", batch.size());
            
            try {
                var products = repositoryTimer.recordCallable(() -> 
                    productRepository.findAllById(batch));
                
                var productTypeIds = products.stream()
                    .map(Product::getProductTypeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
                
                var productTypes = repositoryTimer.recordCallable(() -> 
                    productTypeRepository.findAllById(productTypeIds)
                        .stream()
                        .collect(Collectors.toMap(
                            ProductType::getId,
                            Function.identity()
                        )));
                
                return products.stream()
                    .map(product -> validateProduct(product, productTypes))
                    .collect(Collectors.toList());
                    
            } catch (Exception e) {
                logger.error("Error processing batch: {}", batch, e);
                errorCounter.increment();
                return batch.stream()
                    .map(id -> ValidationResult.builder()
                        .productId(id)
                        .valid(false)
                        .errorMessage("Batch processing failed: " + e.getMessage())
                        .build())
                    .collect(Collectors.toList());
            }
        }, taskExecutor);
    }
    
    private ValidationResult validateProduct(Product product, Map<String, ProductType> productTypes) {
        var builder = ValidationResult.builder().productId(product.getId());
        
        try {
            // Validate product basic fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return builder.valid(false)
                    .errorMessage("Product name is required")
                    .build();
            }
            
            // Validate product type association
            if (product.getProductTypeId() == null) {
                return builder.valid(false)
                    .errorMessage("Product type is required")
                    .build();
            }
            
            var productType = productTypes.get(product.getProductTypeId());
            if (productType == null) {
                return builder.valid(false)
                    .errorMessage("Invalid product type reference")
                    .build();
            }
            
            // Validate product type specific rules
            if (!isProductCompatibleWithType(product, productType)) {
                return builder.valid(false)
                    .errorMessage("Product attributes incompatible with product type")
                    .build();
            }
            
            logger.trace("Product {} validated successfully", product.getId());
            return builder.valid(true).build();
            
        } catch (Exception e) {
            logger.warn("Validation error for product {}: {}", product.getId(), e.getMessage());
            return builder.valid(false)
                .errorMessage("Validation failed: " + e.getMessage())
                .build();
        }
    }
    
    private boolean isProductCompatibleWithType(Product product, ProductType productType) {
        // Enhanced validation using Java 23 pattern matching
        return switch (productType.getCategory()) {
            case "ELECTRONICS" -> product.getAttributes().containsKey("warranty") &&
                                 product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
            case "CLOTHING" -> product.getAttributes().containsKey("size") &&
                              product.getAttributes().containsKey("material");
            case "BOOKS" -> product.getAttributes().containsKey("isbn") &&
                           product.getAttributes().containsKey("author");
            case null -> {
                logger.warn("Product type {} has null category", productType.getId());
                yield false;
            }
            default -> {
                logger.debug("Using default validation for category: {}", productType.getCategory());
                yield product.getName() != null && !product.getName().trim().isEmpty();
            }
        };
    }
    
    private static <T> List<List<T>> partitionIntoBatches(List<T> list, int batchSize) {
        if (list.isEmpty()) {
            return List.of();
        }
        
        var batches = new ArrayList<List<T>>();
        for (int i = 0; i < list.size(); i += batchSize) {
            var endIndex = Math.min(i + batchSize, list.size());
            batches.add(new ArrayList<>(list.subList(i, endIndex)));
        }
        return batches;
    }
}