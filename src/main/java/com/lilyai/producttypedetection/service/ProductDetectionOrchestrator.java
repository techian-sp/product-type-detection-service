package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.model.BatchSummary;
import com.lilyai.producttypedetection.model.ProductData;
import com.lilyai.producttypedetection.model.ValidationResult;
import com.lilyai.producttypedetection.model.DetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class ProductDetectionOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductDetectionOrchestrator.class);
    
    private final ProductValidationService validationService;
    private final ProductIngestionService ingestionService;
    private final ProductTypeDetectionService detectionService;
    private final Executor taskExecutor;
    
    public ProductDetectionOrchestrator(
            ProductValidationService validationService,
            ProductIngestionService ingestionService,
            ProductTypeDetectionService detectionService,
            Executor taskExecutor) {
        this.validationService = validationService;
        this.ingestionService = ingestionService;
        this.detectionService = detectionService;
        this.taskExecutor = taskExecutor;
    }
    
    @Transactional
    public CompletableFuture<BatchSummary> orchestrateDetection(List<ProductData> products, String batchId) {
        logger.info("Starting product detection orchestration for batch: {} with {} products", batchId, products.size());
        
        var startTime = Instant.now();
        
        return CompletableFuture
            .supplyAsync(() -> validateProducts(products, batchId), taskExecutor)
            .thenCompose(validationResults -> {
                var validProducts = validationResults.stream()
                    .filter(ValidationResult::isValid)
                    .map(ValidationResult::productData)
                    .toList();
                
                logger.info("Validation completed for batch: {}. Valid products: {}/{}", 
                    batchId, validProducts.size(), products.size());
                
                return ingestProducts(validProducts, batchId)
                    .thenCompose(ingestionResults -> detectProductTypes(validProducts, batchId))
                    .thenApply(detectionResults -> createBatchSummary(
                        batchId, startTime, validationResults, detectionResults));
            })
            .whenComplete((summary, throwable) -> {
                if (throwable != null) {
                    logger.error("Error during orchestration for batch: {}", batchId, throwable);
                } else {
                    logger.info("Orchestration completed for batch: {} in {} ms", 
                        batchId, summary.processingTimeMs());
                }
            });
    }
    
    private List<ValidationResult> validateProducts(List<ProductData> products, String batchId) {
        logger.debug("Starting validation phase for batch: {}", batchId);
        
        var results = products.parallelStream()
            .map(product -> {
                try {
                    return validationService.validate(product);
                } catch (Exception e) {
                    logger.warn("Validation failed for product: {} in batch: {}", 
                        product.id(), batchId, e);
                    return ValidationResult.invalid(product, e.getMessage());
                }
            })
            .collect(Collectors.toList());
        
        logger.debug("Validation phase completed for batch: {}", batchId);
        return results;
    }
    
    private CompletableFuture<Void> ingestProducts(List<ProductData> products, String batchId) {
        logger.debug("Starting ingestion phase for batch: {} with {} valid products", 
            batchId, products.size());
        
        return CompletableFuture
            .runAsync(() -> {
                try {
                    ingestionService.ingestBatch(products, batchId);
                    logger.debug("Ingestion phase completed for batch: {}", batchId);
                } catch (Exception e) {
                    logger.error("Ingestion failed for batch: {}", batchId, e);
                    throw new RuntimeException("Ingestion failed for batch: " + batchId, e);
                }
            }, taskExecutor);
    }
    
    private CompletableFuture<List<DetectionResult>> detectProductTypes(List<ProductData> products, String batchId) {
        logger.debug("Starting detection phase for batch: {} with {} products", 
            batchId, products.size());
        
        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    var results = products.parallelStream()
                        .map(product -> {
                            try {
                                return detectionService.detectType(product);
                            } catch (Exception e) {
                                logger.warn("Detection failed for product: {} in batch: {}", 
                                    product.id(), batchId, e);
                                return DetectionResult.failed(product.id(), e.getMessage());
                            }
                        })
                        .collect(Collectors.toList());
                    
                    logger.debug("Detection phase completed for batch: {}", batchId);
                    return results;
                } catch (Exception e) {
                    logger.error("Detection phase failed for batch: {}", batchId, e);
                    throw new RuntimeException("Detection failed for batch: " + batchId, e);
                }
            }, taskExecutor);
    }
    
    private BatchSummary createBatchSummary(
            String batchId,
            Instant startTime,
            List<ValidationResult> validationResults,
            List<DetectionResult> detectionResults) {
        
        var endTime = Instant.now();
        var processingTime = endTime.toEpochMilli() - startTime.toEpochMilli();
        
        var totalProducts = validationResults.size();
        var validProducts = (int) validationResults.stream().filter(ValidationResult::isValid).count();
        var successfulDetections = (int) detectionResults.stream().filter(DetectionResult::isSuccess).count();
        var failedDetections = detectionResults.size() - successfulDetections;
        
        var summary = new BatchSummary(
            batchId,
            totalProducts,
            validProducts,
            successfulDetections,
            failedDetections,
            processingTime,
            startTime,
            endTime
        );
        
        logger.info("Batch summary created for {}: {} total, {} valid, {} detected, {} failed, {} ms",
            batchId, totalProducts, validProducts, successfulDetections, failedDetections, processingTime);
        
        return summary;
    }
}