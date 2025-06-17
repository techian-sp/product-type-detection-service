package com.lilyai.producttypedetection.service;

import com.lilyai.producttypedetection.model.Product;
import com.lilyai.producttypedetection.model.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductTypeDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductTypeDetectionService.class);
    
    private static final Map<String, ProductType> KEYWORD_MAPPINGS = Map.of(
        "laptop", ProductType.ELECTRONICS,
        "phone", ProductType.ELECTRONICS,
        "shirt", ProductType.CLOTHING,
        "book", ProductType.BOOKS,
        "chair", ProductType.FURNITURE,
        "table", ProductType.FURNITURE
    );
    
    @Cacheable(value = "productTypeCache", key = "#product.id")
    public ProductType detectProductType(Product product) {
        logger.info("Starting product type detection for product: {}", product.id());
        
        try {
            logger.debug("Analyzing product name: '{}' and description: '{}'", 
                product.name(), product.description());
            
            // Simulate classification processing time
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
            
            ProductType detectedType = classifyProduct(product);
            
            logger.info("Product type detection completed. Product ID: {}, Detected Type: {}", 
                product.id(), detectedType);
            
            return detectedType;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Product type detection interrupted for product: {}", product.id(), e);
            return ProductType.UNKNOWN;
        } catch (Exception e) {
            logger.error("Error during product type detection for product: {}", product.id(), e);
            return ProductType.UNKNOWN;
        }
    }
    
    private ProductType classifyProduct(Product product) {
        logger.debug("Running stub classifier for product: {}", product.id());
        
        String combinedText = STR."\{product.name()} \{product.description()}".toLowerCase();
        
        for (var entry : KEYWORD_MAPPINGS.entrySet()) {
            if (combinedText.contains(entry.getKey())) {
                logger.debug("Keyword '{}' matched, classifying as: {}", 
                    entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        
        // Fallback classification based on price range
        return switch (product.price()) {
            case var price when price > 1000.0 -> {
                logger.debug("High price detected ({}), classifying as ELECTRONICS", price);
                yield ProductType.ELECTRONICS;
            }
            case var price when price > 100.0 -> {
                logger.debug("Medium price detected ({}), classifying as FURNITURE", price);
                yield ProductType.FURNITURE;
            }
            default -> {
                logger.debug("Low price detected ({}), classifying as BOOKS", product.price());
                yield ProductType.BOOKS;
            }
        };
    }
    
    @Cacheable(value = "batchProductTypeCache", key = "#products.hashCode()")
    public List<ProductType> detectProductTypes(List<Product> products) {
        logger.info("Starting batch product type detection for {} products", products.size());
        
        var results = products.stream()
            .map(this::detectProductType)
            .toList();
        
        logger.info("Batch product type detection completed for {} products", products.size());
        return results;
    }
}