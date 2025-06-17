package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.model.ProductType;
import com.lilyai.producttypedetection.metrics.ProcessingMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
class ProductBatchProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductBatchProcessorTest.class);
    
    @Mock
    private MeterRegistry meterRegistry;
    
    @Mock
    private Counter processedCounter;
    
    @Mock
    private Counter duplicateCounter;
    
    @Mock
    private Counter validationErrorCounter;
    
    @Mock
    private Timer processingTimer;
    
    @Mock
    private Timer.Sample timerSample;
    
    @Mock
    private Validator validator;
    
    private ProductBatchProcessor processor;
    private ProcessingMetrics metrics;
    
    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("products.processed")).thenReturn(processedCounter);
        when(meterRegistry.counter("products.duplicates")).thenReturn(duplicateCounter);
        when(meterRegistry.counter("products.validation.errors")).thenReturn(validationErrorCounter);
        when(meterRegistry.timer("products.processing.time")).thenReturn(processingTimer);
        when(processingTimer.start()).thenReturn(timerSample);
        
        metrics = new ProcessingMetrics(meterRegistry);
        processor = new ProductBatchProcessor(validator, metrics);
    }
    
    @Test
    void testBatchProcessingWithValidProducts() {
        // Given
        var products = createValidProductBatch(100);
        when(validator.validate(any(Product.class))).thenReturn(Set.of());
        
        // When
        var result = processor.processBatch(products);
        
        // Then
        assertThat(result).hasSize(100);
        verify(processedCounter, times(100)).increment();
        verify(timerSample).stop(processingTimer);
        
        logger.info("Successfully processed batch of {} products", result.size());
    }
    
    @Test
    void testDuplicateFiltering() {
        // Given
        var products = new ArrayList<Product>();
        var duplicateProduct = new Product("PROD-001", "Laptop", "Electronics", 999.99, Instant.now());
        
        // Add same product multiple times
        IntStream.range(0, 5).forEach(i -> products.add(duplicateProduct));
        
        // Add unique products
        products.addAll(createValidProductBatch(10));
        
        when(validator.validate(any(Product.class))).thenReturn(Set.of());
        
        // When
        var result = processor.processBatch(products);
        
        // Then
        assertThat(result).hasSize(11); // 1 unique duplicate + 10 unique products
        verify(duplicateCounter, times(4)).increment(); // 4 duplicates filtered
        
        logger.info("Filtered {} duplicates from batch", 4);
    }
    
    @Test
    void testValidationErrorHandling() {
        // Given
        var products = createInvalidProductBatch(5);
        var violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid product name");
        when(validator.validate(any(Product.class))).thenReturn(Set.of(violation));
        
        // When
        var result = processor.processBatch(products);
        
        // Then
        assertThat(result).isEmpty();
        verify(validationErrorCounter, times(5)).increment();
        
        logger.warn("Validation failed for {} products", 5);
    }
    
    @Test
    void testConcurrentBatchProcessing() {
        // Given
        var batches = List.of(
            createValidProductBatch(50),
            createValidProductBatch(75),
            createValidProductBatch(25)
        );
        
        when(validator.validate(any(Product.class))).thenReturn(Set.of());
        
        // When
        var futures = batches.stream()
            .map(batch -> CompletableFuture.supplyAsync(() -> processor.processBatch(batch)))
            .toList();
        
        var results = futures.stream()
            .map(CompletableFuture::join)
            .toList();
        
        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0)).hasSize(50);
        assertThat(results.get(1)).hasSize(75);
        assertThat(results.get(2)).hasSize(25);
        
        verify(processedCounter, times(150)).increment();
        
        logger.info("Processed {} concurrent batches successfully", batches.size());
    }
    
    @Test
    void testMetricsUpdateAccuracy() {
        // Given
        var validProducts = createValidProductBatch(10);
        var invalidProducts = createInvalidProductBatch(3);
        var allProducts = new ArrayList<Product>();
        allProducts.addAll(validProducts);
        allProducts.addAll(invalidProducts);
        
        // Add duplicates
        allProducts.add(validProducts.get(0));
        allProducts.add(validProducts.get(1));
        
        when(validator.validate(argThat(p -> validProducts.contains(p))))
            .thenReturn(Set.of());
        
        var violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error");
        when(validator.validate(argThat(p -> invalidProducts.contains(p))))
            .thenReturn(Set.of(violation));
        
        // When
        var result = processor.processBatch(allProducts);
        
        // Then
        assertThat(result).hasSize(10); // Only valid, unique products
        verify(processedCounter, times(10)).increment();
        verify(duplicateCounter, times(2)).increment();
        verify(validationErrorCounter, times(3)).increment();
        
        logger.info("Metrics updated: processed={}, duplicates={}, validation_errors={}", 
                   10, 2, 3);
    }
    
    @Test
    void testLargeScaleBatchProcessing() {
        // Given
        var largeProductBatch = createValidProductBatch(10000);
        when(validator.validate(any(Product.class))).thenReturn(Set.of());
        
        var startTime = System.currentTimeMillis();
        
        // When
        var result = processor.processBatch(largeProductBatch);
        
        var endTime = System.currentTimeMillis();
        var processingTime = endTime - startTime;
        
        // Then
        assertThat(result).hasSize(10000);
        assertThat(processingTime).isLessThan(5000); // Should process within 5 seconds
        
        verify(processedCounter, times(10000)).increment();
        
        logger.info("Large scale batch processing completed in {}ms for {} products", 
                   processingTime, result.size());
    }
    
    @Test
    void testEmptyBatchHandling() {
        // Given
        var emptyBatch = List.<Product>of();
        
        // When
        var result = processor.processBatch(emptyBatch);
        
        // Then
        assertThat(result).isEmpty();
        verify(processedCounter, never()).increment();
        verify(timerSample).stop(processingTimer);
        
        logger.info("Empty batch processed successfully");
    }
    
    private List<Product> createValidProductBatch(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> new Product(
                "PROD-" + String.format("%06d", i),
                "Product " + i,
                ProductType.ELECTRONICS.name(),
                99.99 + i,
                Instant.now().minusSeconds(i)
            ))
            .toList();
    }
    
    private List<Product> createInvalidProductBatch(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> new Product(
                "", // Invalid empty ID
                null, // Invalid null name
                "INVALID_CATEGORY",
                -1.0, // Invalid negative price
                null // Invalid null timestamp
            ))
            .toList();
    }
    
    static class ProductBatchProcessor {
        private final Validator validator;
        private final ProcessingMetrics metrics;
        private final Set<String> processedIds = ConcurrentHashMap.newKeySet();
        
        public ProductBatchProcessor(Validator validator, ProcessingMetrics metrics) {
            this.validator = validator;
            this.metrics = metrics;
        }
        
        public List<Product> processBatch(List<Product> products) {
            var timer = metrics.startProcessingTimer();
            
            try {
                return products.parallelStream()
                    .filter(this::filterDuplicates)
                    .filter(this::validateProduct)
                    .peek(p -> metrics.incrementProcessed())
                    .toList();
            } finally {
                timer.stop();
            }
        }
        
        private boolean filterDuplicates(Product product) {
            if (processedIds.add(product.id())) {
                return true;
            } else {
                metrics.incrementDuplicates();
                logger.debug("Filtered duplicate product: {}", product.id());
                return false;
            }
        }
        
        private boolean validateProduct(Product product) {
            var violations = validator.validate(product);
            if (violations.isEmpty()) {
                return true;
            } else {
                metrics.incrementValidationErrors();
                logger.warn("Validation failed for product {}: {}", 
                           product.id(), 
                           violations.stream()
                               .map(ConstraintViolation::getMessage)
                               .toList());
                return false;
            }
        }
    }
}