package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.classifier.ProductClassifier;
import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig(ProductTypeDetectionServiceTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProductTypeDetectionServiceTest {

    @Mock
    private ProductClassifier productClassifier;
    
    private ProductTypeDetectionService detectionService;
    private CacheManager cacheManager;
    
    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("productTypes");
        detectionService = new ProductTypeDetectionService(productClassifier, cacheManager);
    }
    
    @Test
    void shouldReturnStubResponseForUnknownProduct() {
        // Given
        var product = new Product("unknown-123", "Mystery Item", "Unknown product description");
        when(productClassifier.classify(any(Product.class))).thenReturn(ProductType.UNKNOWN);
        
        // When
        var result = detectionService.detectProductType(product);
        
        // Then
        assertThat(result).isEqualTo(ProductType.UNKNOWN);
        verify(productClassifier, times(1)).classify(product);
    }
    
    @Test
    void shouldCacheDetectionResults() {
        // Given
        var product = new Product("electronics-456", "Smartphone", "Latest model smartphone");
        when(productClassifier.classify(any(Product.class))).thenReturn(ProductType.ELECTRONICS);
        
        // When - First call
        var firstResult = detectionService.detectProductType(product);
        
        // When - Second call with same product
        var secondResult = detectionService.detectProductType(product);
        
        // Then
        assertThat(firstResult).isEqualTo(ProductType.ELECTRONICS);
        assertThat(secondResult).isEqualTo(ProductType.ELECTRONICS);
        
        // Verify classifier was called only once due to caching
        verify(productClassifier, times(1)).classify(product);
        
        // Verify cache contains the result
        var cache = cacheManager.getCache("productTypes");
        assertThat(cache).isNotNull();
        assertThat(cache.get(product.id())).isNotNull();
    }
    
    @Test
    void shouldNotRepeatDetectionForSameProductId() {
        // Given
        var productId = "clothing-789";
        var product1 = new Product(productId, "T-Shirt", "Cotton t-shirt");
        var product2 = new Product(productId, "Updated T-Shirt", "Premium cotton t-shirt");
        
        when(productClassifier.classify(any(Product.class))).thenReturn(ProductType.CLOTHING);
        
        // When
        var result1 = detectionService.detectProductType(product1);
        var result2 = detectionService.detectProductType(product2); // Same ID, different details
        
        // Then
        assertThat(result1).isEqualTo(ProductType.CLOTHING);
        assertThat(result2).isEqualTo(ProductType.CLOTHING);
        
        // Verify classifier was called only once for the same product ID
        verify(productClassifier, times(1)).classify(any(Product.class));
    }
    
    @Test
    void shouldHandleConcurrentDetectionRequests() throws Exception {
        // Given
        var product = new Product("books-101", "Java Programming", "Advanced Java concepts");
        when(productClassifier.classify(any(Product.class))).thenReturn(ProductType.BOOKS);
        
        // When - Simulate concurrent requests
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = List.of(
                CompletableFuture.supplyAsync(() -> detectionService.detectProductType(product), executor),
                CompletableFuture.supplyAsync(() -> detectionService.detectProductType(product), executor),
                CompletableFuture.supplyAsync(() -> detectionService.detectProductType(product), executor),
                CompletableFuture.supplyAsync(() -> detectionService.detectProductType(product), executor)
            );
            
            var results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            // Then
            assertThat(results).hasSize(4);
            assertThat(results).allMatch(result -> result == ProductType.BOOKS);
            
            // Verify classifier was called minimal times due to caching
            verify(productClassifier, atMost(2)).classify(product);
        }
    }
    
    @Test
    void shouldValidateMultipleProductTypesWithoutRepeatedDetection() {
        // Given
        var products = List.of(
            new Product("home-001", "Vacuum Cleaner", "High-power vacuum"),
            new Product("sports-002", "Tennis Racket", "Professional tennis racket"),
            new Product("home-001", "Vacuum Cleaner V2", "Updated vacuum model") // Same ID as first
        );
        
        when(productClassifier.classify(argThat(p -> p.id().equals("home-001"))))
            .thenReturn(ProductType.HOME_GARDEN);
        when(productClassifier.classify(argThat(p -> p.id().equals("sports-002"))))
            .thenReturn(ProductType.SPORTS);
        
        // When
        var results = products.stream()
            .map(detectionService::detectProductType)
            .toList();
        
        // Then
        assertThat(results).containsExactly(
            ProductType.HOME_GARDEN,
            ProductType.SPORTS,
            ProductType.HOME_GARDEN // Cached result for same ID
        );
        
        // Verify each unique product ID was classified only once
        verify(productClassifier, times(1)).classify(argThat(p -> p.id().equals("home-001")));
        verify(productClassifier, times(1)).classify(argThat(p -> p.id().equals("sports-002")));
        verify(productClassifier, times(2)).classify(any(Product.class)); // Total calls
    }
    
    @Configuration
    @EnableCaching
    static class TestConfig {
        
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("productTypes");
        }
    }
}